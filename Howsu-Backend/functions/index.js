const functions = require("firebase-functions");
const admin = require("firebase-admin");
const axios = require("axios");

// Firebase Admin SDK 초기화
admin.initializeApp();

// ------------------------------------------------------------------
// 1. 네이버 로그인 함수
// ------------------------------------------------------------------
/**
 * 네이버 액세스 토큰을 받아 Firebase 커스텀 토큰을 생성
 */
exports.verifyNaverToken = functions.https.onCall(async (data, context) => {
  const naverAccessToken = data.naverAccessToken;
  if (!naverAccessToken) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Naver Access Token이 없습니다."
    );
  }

  try {
    const response = await axios.get("https://openapi.naver.com/v1/nid/me", {
      headers: { Authorization: `Bearer ${naverAccessToken}` },
    });

    const naverUserId = response.data.response.id;
    if (!naverUserId) {
      throw new functions.https.HttpsError(
        "internal",
        "네이버 사용자 ID를 가져오지 못했습니다."
      );
    }

    const firebaseUid = `naver:${naverUserId}`;
    const firebaseCustomToken = await admin
      .auth()
      .createCustomToken(firebaseUid);

    return { firebaseCustomToken };
  } catch (error) {
    console.error("네이버 인증 실패:", error.message);
    throw new functions.https.HttpsError(
      "unauthenticated",
      "네이버 토큰 검증에 실패했습니다."
    );
  }
});

// ------------------------------------------------------------------
// 2. 카카오 로그인 함수
// ------------------------------------------------------------------
exports.kakaoLogin = functions.https.onCall(async (data, context) => {
  const kakaoAccessToken = data.token;
  if (!kakaoAccessToken) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "카카오 토큰이 없습니다."
    );
  }

  let kakaoProfile;
  try {
    const response = await axios.get("https://kapi.kakao.com/v2/user/me", {
      headers: { Authorization: `Bearer ${kakaoAccessToken}` },
    });
    kakaoProfile = response.data;
  } catch (error) {
    console.error("카카오 토큰 검증 실패:", error.message);
    throw new functions.https.HttpsError(
      "unauthenticated",
      "카카오 토큰이 유효하지 않습니다."
    );
  }

  const kakaoId = kakaoProfile.id;
  const email = kakaoProfile.kakao_account?.email;
  const nickname = kakaoProfile.properties?.nickname;
  const profileImage = kakaoProfile.properties?.profile_image;

  if (!kakaoId || !email) {
    throw new functions.https.HttpsError(
      "data-loss",
      "카카오에서 이메일 또는 ID를 가져오지 못했습니다."
    );
  }

  const firebaseUser = await getOrCreateFirebaseUser(
    kakaoId.toString(),
    email,
    nickname,
    profileImage
  );

  const customToken = await admin.auth().createCustomToken(firebaseUser.uid);

  return { firebaseToken: customToken };
});

// ------------------------------------------------------------------
// 헬퍼 함수
// ------------------------------------------------------------------
const getOrCreateFirebaseUser = async (
  kakaoId,
  email,
  displayName,
  photoURL
) => {
  try {
    const userRecord = await admin.auth().getUserByEmail(email);
    return userRecord;
  } catch (error) {
    if (error.code === "auth/user-not-found") {
      const userRecord = await admin.auth().createUser({
        email,
        displayName,
        photoURL,
        uid: `kakao:${kakaoId}`,
      });
      return userRecord;
    }
    throw error;
  }
};
