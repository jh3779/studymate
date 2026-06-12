package com.example.studymate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import com.example.studymate.validation.StudyInputValidator;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudyInputActivity extends BaseActivity {

    private EditText titleInput;
    private EditText subjectInput;
    private EditText contentInput;
    private TextView errorText;
    private TextView importStatusText;
    private TextView loadingBox;
    private Button generateButton;
    private Button importPdfButton;
    private Button importImageButton;

    private final ExecutorService importExecutor = Executors.newSingleThreadExecutor();
    private ActivityResultLauncher<String[]> pdfPickerLauncher;
    private ActivityResultLauncher<String[]> imagePickerLauncher;
    private StudyInputViewModel viewModel;
    private boolean loading;
    private boolean importing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PDFBoxResourceLoader.init(getApplicationContext());
        registerImportLaunchers();
        setContentView(R.layout.activity_study_input);

        titleInput = findViewById(R.id.titleInput);
        subjectInput = findViewById(R.id.subjectInput);
        contentInput = findViewById(R.id.contentInput);
        errorText = findViewById(R.id.inputErrorText);
        importStatusText = findViewById(R.id.importStatusText);
        loadingBox = findViewById(R.id.loadingBox);
        generateButton = findViewById(R.id.generateSummaryButton);
        importPdfButton = findViewById(R.id.importPdfButton);
        importImageButton = findViewById(R.id.importImageButton);
        viewModel = new ViewModelProvider(this).get(StudyInputViewModel.class);
        viewModel.getState().observe(this, this::renderOperationState);

        bindClick(R.id.backHome, v -> finish());
        bindClick(R.id.generateSummaryButton, v -> handleGenerateSummary());
        bindClick(R.id.importPdfButton, v -> pdfPickerLauncher.launch(new String[]{"application/pdf"}));
        bindClick(R.id.importImageButton, v -> imagePickerLauncher.launch(new String[]{"image/*"}));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        importExecutor.shutdownNow();
    }

    private void registerImportLaunchers() {
        pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) extractPdfText(uri);
                }
        );
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) extractImageText(uri);
                }
        );
    }

    private void handleGenerateSummary() {
        String title = titleInput.getText().toString().trim();
        String subject = subjectInput.getText().toString().trim();
        String content = contentInput.getText().toString().trim();

        StudyInputValidator.ValidationResult validation =
                StudyInputValidator.validate(title, content);
        if (!validation.isValid()) {
            showError("⚠ " + validation.getMessage());
            return;
        }

        errorText.setVisibility(View.GONE);
        viewModel.generateSummary(title, subject, content);
    }

    private void renderOperationState(StudyInputViewModel.State state) {
        switch (state.status) {
            case GENERATING:
                setLoading(
                        true,
                        "⏳ AI 요약 생성 중...\n잠시만 기다려주세요",
                        "AI 요약 생성 중..."
                );
                break;
            case SAVING:
                setLoading(
                        true,
                        "⏳ 학습 기록 저장 중...\n잠시만 기다려주세요",
                        "학습 기록 저장 중..."
                );
                break;
            case SUCCESS:
                setLoading(false);
                StudyInputViewModel.Result result = state.result;
                viewModel.consumeSuccess();
                Intent intent = new Intent(StudyInputActivity.this, SummaryResultActivity.class);
                intent.putExtra("noteId", result.noteId);
                intent.putExtra("title", result.title);
                intent.putExtra("subject", result.subject);
                intent.putExtra("content", result.content);
                intent.putStringArrayListExtra("summary", result.summary);
                intent.putStringArrayListExtra("keywords", result.keywords);
                startActivity(intent);
                break;
            case ERROR:
                setLoading(false);
                showError("⚠ " + state.errorMessage);
                break;
            case IDLE:
            default:
                setLoading(false);
                break;
        }
    }

    private void setLoading(boolean loading) {
        setLoading(
                loading,
                "⏳ AI 요약 생성 중...\n잠시만 기다려주세요",
                "AI 요약 생성 중..."
        );
    }

    private void setLoading(boolean loading, String loadingText, String buttonText) {
        this.loading = loading;
        loadingBox.setVisibility(loading ? View.VISIBLE : View.GONE);
        loadingBox.setText(loading
                ? loadingText
                : "⏳ AI 요약 생성 중...\n잠시만 기다려주세요");
        generateButton.setText(loading ? buttonText : "AI 요약 생성");
        updateInputEnabledState();
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void extractPdfText(Uri uri) {
        setImporting(true, "PDF 텍스트를 추출하는 중입니다.");
        importExecutor.execute(() -> {
            try (InputStream input = getContentResolver().openInputStream(uri);
                 PDDocument document = PDDocument.load(input)) {
                String text = new PDFTextStripper().getText(document);
                runOnUiThread(() -> applyExtractedText(text, "PDF"));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setImporting(false, "");
                    showError("⚠ PDF 텍스트 추출에 실패했습니다. 텍스트 PDF인지 확인하거나 스캔 자료는 이미지 OCR로 가져와주세요.");
                });
            }
        });
    }

    private void extractImageText(Uri uri) {
        setImporting(true, "이미지에서 텍스트를 인식하는 중입니다.");
        try {
            InputImage image = InputImage.fromFilePath(this, uri);
            TextRecognizer recognizer = TextRecognition.getClient(
                    new KoreanTextRecognizerOptions.Builder().build()
            );
            recognizer.process(image)
                    .addOnSuccessListener(result -> {
                        recognizer.close();
                        applyExtractedText(result.getText(), "이미지");
                    })
                    .addOnFailureListener(error -> {
                        recognizer.close();
                        setImporting(false, "");
                        showError("⚠ 이미지 OCR에 실패했습니다. 글자가 선명한 이미지를 선택해주세요.");
                    });
        } catch (Exception e) {
            setImporting(false, "");
            showError("⚠ 이미지를 불러오지 못했습니다.");
        }
    }

    private void applyExtractedText(String text, String sourceLabel) {
        setImporting(false, "");
        String normalized = text == null ? "" : text.trim();
        if (normalized.length() < 30) {
            showError("⚠ " + sourceLabel + "에서 30자 이상의 학습 내용을 찾지 못했습니다. 스캔 PDF는 이미지로 변환한 뒤 이미지 OCR을 사용해주세요.");
            return;
        }
        if (normalized.length() > 5000) {
            normalized = normalized.substring(0, 5000);
            showImportStatus(sourceLabel + " 텍스트가 5000자를 넘어 앞부분만 가져왔습니다.");
        } else {
            showImportStatus(sourceLabel + " 텍스트를 학습 내용에 추가했습니다.");
        }
        contentInput.setText(normalized);
        contentInput.setSelection(contentInput.getText().length());
        errorText.setVisibility(View.GONE);
    }

    private void setImporting(boolean importing, String message) {
        this.importing = importing;
        updateInputEnabledState();
        if (importing) {
            showImportStatus(message);
        }
    }

    private void updateInputEnabledState() {
        boolean enabled = !loading && !importing;
        titleInput.setEnabled(enabled);
        subjectInput.setEnabled(enabled);
        contentInput.setEnabled(enabled);
        generateButton.setEnabled(enabled);
        importPdfButton.setEnabled(enabled);
        importImageButton.setEnabled(enabled);
    }

    private void showImportStatus(String message) {
        importStatusText.setText(message);
        importStatusText.setVisibility(message == null || message.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
