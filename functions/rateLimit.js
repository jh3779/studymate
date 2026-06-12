const DEFAULT_LIMIT = 12;
const DEFAULT_WINDOW_MS = 10 * 60 * 1000;

function nextRateLimitState(current, nowMs, limit, windowMs) {
  const windowStart = Number(current?.windowStart);
  const count = Number(current?.count);
  const hasActiveWindow =
    Number.isFinite(windowStart) &&
    Number.isFinite(count) &&
    count >= 0 &&
    nowMs - windowStart < windowMs;

  if (!hasActiveWindow) {
    return {
      allowed: true,
      count: 1,
      windowStart: nowMs,
      retryAfterSeconds: 0,
    };
  }

  if (count >= limit) {
    return {
      allowed: false,
      count,
      windowStart,
      retryAfterSeconds: Math.max(
        1,
        Math.ceil((windowStart + windowMs - nowMs) / 1000)
      ),
    };
  }

  return {
    allowed: true,
    count: count + 1,
    windowStart,
    retryAfterSeconds: 0,
  };
}

function createAiRateLimiter(
  admin,
  {
    limit = DEFAULT_LIMIT,
    windowMs = DEFAULT_WINDOW_MS,
    now = () => Date.now(),
  } = {}
) {
  const firestore = admin.firestore();

  return async function aiRateLimiter(req, res, next) {
    const uid = req.user?.uid;
    if (!uid) {
      return res.status(401).json({ error: "인증이 필요합니다." });
    }

    const document = firestore.collection("ai_rate_limits").doc(uid);

    try {
      const result = await firestore.runTransaction(async (transaction) => {
        const snapshot = await transaction.get(document);
        const decision = nextRateLimitState(
          snapshot.exists ? snapshot.data() : null,
          now(),
          limit,
          windowMs
        );

        if (decision.allowed) {
          transaction.set(
            document,
            {
              count: decision.count,
              windowStart: decision.windowStart,
              updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            },
            { merge: true }
          );
        }

        return decision;
      });

      if (!result.allowed) {
        res.set("Retry-After", String(result.retryAfterSeconds));
        return res.status(429).json({
          error: "AI 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.",
        });
      }

      return next();
    } catch (error) {
      console.error("AI 요청 제한 확인 실패:", error.message ?? error);
      return res.status(503).json({
        error: "AI 요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.",
      });
    }
  };
}

module.exports = {
  DEFAULT_LIMIT,
  DEFAULT_WINDOW_MS,
  createAiRateLimiter,
  nextRateLimitState,
};
