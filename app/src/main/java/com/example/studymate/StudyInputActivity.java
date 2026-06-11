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

import com.example.studymate.model.StudyNoteModel;
import com.example.studymate.service.AiService;
import com.example.studymate.service.AuthService;
import com.example.studymate.service.FirestoreService;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.util.ArrayList;
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

    private final AiService aiService = new AiService();
    private final AuthService authService = new AuthService();
    private final FirestoreService firestoreService = new FirestoreService();
    private final ExecutorService importExecutor = Executors.newSingleThreadExecutor();
    private ActivityResultLauncher<String[]> pdfPickerLauncher;
    private ActivityResultLauncher<String[]> imagePickerLauncher;

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

        if (title.isEmpty()) {
            showError("⚠ 제목을 입력해주세요.");
            return;
        }
        if (content.length() < 30) {
            showError("⚠ 학습 내용은 30자 이상 입력해주세요. 현재 " + content.length() + "자입니다.");
            return;
        }
        if (content.length() > 5000) {
            showError("⚠ 학습 내용은 5000자 이하로 입력해주세요. 현재 " + content.length() + "자입니다.");
            return;
        }

        errorText.setVisibility(View.GONE);
        loadingBox.setVisibility(View.VISIBLE);
        generateButton.setEnabled(false);
        generateButton.setText("AI 요약 생성 중...");

        aiService.generateSummary(content, new AiService.SummaryCallback() {
            @Override
            public void onSuccess(AiService.SummaryResult result) {
                saveStudyNote(title, subject, content, result);
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                showError("⚠ " + errorMessage);
            }
        });
    }

    private void saveStudyNote(
            String title,
            String subject,
            String content,
            AiService.SummaryResult result
    ) {
        String userId = authService.getCurrentUserId();
        if (userId == null) {
            setLoading(false);
            showError("⚠ 로그인이 만료되었습니다. 다시 로그인해주세요.");
            return;
        }

        loadingBox.setText("⏳ 학습 기록 저장 중...\n잠시만 기다려주세요");
        generateButton.setText("학습 기록 저장 중...");

        StudyNoteModel note = new StudyNoteModel(
                null,
                userId,
                title,
                subject,
                content,
                result.summary,
                result.keywords,
                null
        );

        firestoreService.saveStudyNote(note, new FirestoreService.SaveCallback() {
            @Override
            public void onSuccess(String documentId) {
                setLoading(false);

                Intent intent = new Intent(StudyInputActivity.this, SummaryResultActivity.class);
                intent.putExtra("noteId", documentId);
                intent.putExtra("title", title);
                intent.putExtra("subject", subject);
                intent.putExtra("content", content);
                intent.putStringArrayListExtra("summary", new ArrayList<>(result.summary));
                intent.putStringArrayListExtra("keywords", new ArrayList<>(result.keywords));
                startActivity(intent);
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                showError("⚠ 요약은 생성됐지만 저장에 실패했습니다. " + errorMessage);
            }
        });
    }

    private void setLoading(boolean loading) {
        loadingBox.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (!loading) {
            loadingBox.setText("⏳ AI 요약 생성 중...\n잠시만 기다려주세요");
        }
        generateButton.setEnabled(!loading);
        generateButton.setText(loading ? "AI 요약 생성 중..." : "AI 요약 생성");
        titleInput.setEnabled(!loading);
        subjectInput.setEnabled(!loading);
        contentInput.setEnabled(!loading);
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
                    showError("⚠ PDF 텍스트 추출에 실패했습니다. 텍스트가 포함된 PDF인지 확인해주세요.");
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
            showError("⚠ " + sourceLabel + "에서 30자 이상의 학습 내용을 찾지 못했습니다.");
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
        titleInput.setEnabled(!importing);
        subjectInput.setEnabled(!importing);
        contentInput.setEnabled(!importing);
        generateButton.setEnabled(!importing);
        importPdfButton.setEnabled(!importing);
        importImageButton.setEnabled(!importing);
        if (importing) {
            showImportStatus(message);
        }
    }

    private void showImportStatus(String message) {
        importStatusText.setText(message);
        importStatusText.setVisibility(message == null || message.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
