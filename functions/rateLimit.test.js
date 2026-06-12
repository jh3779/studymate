const test = require("node:test");
const assert = require("node:assert/strict");

const {
  nextRateLimitState,
} = require("./rateLimit");

test("starts a new rate-limit window", () => {
  const result = nextRateLimitState(null, 1000, 3, 10_000);

  assert.deepEqual(result, {
    allowed: true,
    count: 1,
    windowStart: 1000,
    retryAfterSeconds: 0,
  });
});

test("increments requests inside the active window", () => {
  const result = nextRateLimitState(
    { count: 1, windowStart: 1000 },
    2000,
    3,
    10_000
  );

  assert.equal(result.allowed, true);
  assert.equal(result.count, 2);
  assert.equal(result.windowStart, 1000);
});

test("blocks requests after the limit and reports retry time", () => {
  const result = nextRateLimitState(
    { count: 3, windowStart: 1000 },
    2500,
    3,
    10_000
  );

  assert.equal(result.allowed, false);
  assert.equal(result.count, 3);
  assert.equal(result.retryAfterSeconds, 9);
});

test("resets an expired window", () => {
  const result = nextRateLimitState(
    { count: 3, windowStart: 1000 },
    11_000,
    3,
    10_000
  );

  assert.equal(result.allowed, true);
  assert.equal(result.count, 1);
  assert.equal(result.windowStart, 11_000);
});
