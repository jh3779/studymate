function createVerifyAuth(admin) {
  return async function verifyAuth(req, res, next) {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith("Bearer ")) {
      return res.status(401).json({ error: "인증이 필요합니다." });
    }

    try {
      const decodedToken = await admin.auth().verifyIdToken(
        authHeader.slice(7).trim()
      );
      if (decodedToken.email_verified !== true) {
        return res.status(403).json({ error: "이메일 인증을 완료해주세요." });
      }

      req.user = decodedToken;
      return next();
    } catch (err) {
      console.error("verifyAuth 실패:", err.message ?? err);
      return res.status(401).json({
        error: "인증에 실패했습니다. 다시 로그인해주세요.",
      });
    }
  };
}

module.exports = { createVerifyAuth };
