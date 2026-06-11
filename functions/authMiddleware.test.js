const assert = require("node:assert/strict");
const test = require("node:test");

const { createVerifyAuth } = require("./authMiddleware");

function createResponse() {
  return {
    statusCode: null,
    body: null,
    status(statusCode) {
      this.statusCode = statusCode;
      return this;
    },
    json(body) {
      this.body = body;
      return this;
    },
  };
}

function createAdmin(verifyIdToken) {
  return {
    auth() {
      return { verifyIdToken };
    },
  };
}

test("rejects a request without a bearer token", async () => {
  const verifyAuth = createVerifyAuth(createAdmin(async () => ({})));
  const req = { headers: {} };
  const res = createResponse();
  let nextCalled = false;

  await verifyAuth(req, res, () => {
    nextCalled = true;
  });

  assert.equal(res.statusCode, 401);
  assert.equal(nextCalled, false);
});

test("rejects an unverified email token", async () => {
  const verifyAuth = createVerifyAuth(createAdmin(async () => ({
    uid: "alice",
    email_verified: false,
  })));
  const req = { headers: { authorization: "Bearer token" } };
  const res = createResponse();
  let nextCalled = false;

  await verifyAuth(req, res, () => {
    nextCalled = true;
  });

  assert.equal(res.statusCode, 403);
  assert.equal(nextCalled, false);
});

test("accepts a verified email token", async () => {
  const decodedToken = {
    uid: "alice",
    email_verified: true,
  };
  const verifyAuth = createVerifyAuth(createAdmin(async () => decodedToken));
  const req = { headers: { authorization: "Bearer token" } };
  const res = createResponse();
  let nextCalled = false;

  await verifyAuth(req, res, () => {
    nextCalled = true;
  });

  assert.equal(res.statusCode, null);
  assert.equal(nextCalled, true);
  assert.equal(req.user, decodedToken);
});

test("rejects an invalid or expired token", async () => {
  const verifyAuth = createVerifyAuth(createAdmin(async () => {
    throw new Error("invalid token");
  }));
  const req = { headers: { authorization: "Bearer token" } };
  const res = createResponse();
  const originalConsoleError = console.error;
  let nextCalled = false;

  console.error = () => {};
  try {
    await verifyAuth(req, res, () => {
      nextCalled = true;
    });
  } finally {
    console.error = originalConsoleError;
  }

  assert.equal(res.statusCode, 401);
  assert.equal(nextCalled, false);
});
