const functions = require("firebase-functions");
const admin = require("firebase-admin");
const axios = require("axios");

// Firebase Admin SDK 초기화
admin.initializeApp();

/**
 * 카카오 액세스 토큰을 받아 Firebase 커스텀 토큰을 생성하는 함수
 */
exports.kakaoLogin = functions.https.onCall(async (data, context) => {
  // 1. 앱에서 보낸 '카카오 액세스 토큰'을 받습니다.
  const kakaoAccessToken = data.token;

  if (!kakaoAccessToken) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "카카오 토큰이 없습니다."
    );
  }

  let kakaoProfile;
  try {
    // 2. 받은 토큰으로 카카오 사용자 정보 API를 호출합니다.
    const response = await axios.get("https://kapi.kakao.com/v2/user/me", {
      headers: {
        "Authorization": `Bearer ${kakaoAccessToken}`,
      },
    });
    kakaoProfile = response.data;
  } catch (error) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "카카오 토큰이 유효하지 않습니다."
    );
  }

  // 3. 카카오에서 사용자 정보(ID, 이메일)를 가져옵니다.
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

  // 4. Firebase Auth에서 이메일로 사용자를 찾거나, 없으면 새로 만듭니다.
  const firebaseUser = await getOrCreateFirebaseUser(
    kakaoId.toString(),
    email,
    nickname,
    profileImage
  );

  // 5. Firebase '커스텀 토큰'을 생성합니다.
  const customToken = await admin.auth().createCustomToken(firebaseUser.uid);

  // 6. 생성된 커스텀 토큰을 안드로이드 앱으로 돌려줍니다.
  return {
    firebaseToken: customToken,
  };
});

/**
 * Firebase Auth에서 사용자를 찾거나 생성하는 헬퍼 함수
 */
const getOrCreateFirebaseUser = async (
  kakaoId,
  email,
  displayName,
  photoURL
) => {
  try {
    // 이메일로 사용자를 먼저 찾아봅니다.
    const userRecord = await admin.auth().getUserByEmail(email);
    return userRecord;
  } catch (error) {
    // 사용자가 없으면(code: 'auth/user-not-found'), 새로 생성합니다.
    if (error.code === "auth/user-not-found") {
      const userRecord = await admin.auth().createUser({
        email: email,
        displayName: displayName,
        photoURL: photoURL,
        uid: `kakao:${kakaoId}`, // (고유 ID를 'kakao:1234' 등으로 지정)
      });
      return userRecord;
    }
    // 다른 에러면 그대로 던집니다.
    throw error;
  }
};