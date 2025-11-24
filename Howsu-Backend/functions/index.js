const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { setGlobalOptions } = require("firebase-functions/v2");

const admin = require("firebase-admin");
const axios = require("axios");

admin.initializeApp();

setGlobalOptions({ region: "asia-northeast3" });

// ------------------------------------------------------------------
// 1. 네이버 로그인 함수
// ------------------------------------------------------------------
/**
 * 네이버 액세스 토큰을 받아 Firebase 커스텀 토큰을 생성
 */
exports.verifyNaverToken = onCall(async (data, context) => {
  const naverAccessToken = data.naverAccessToken;
  if (!naverAccessToken) {
    throw new HttpsError("invalid-argument", "Naver Access Token이 없습니다.");
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
exports.kakaoLogin = onCall(async (data, context) => {
  const kakaoAccessToken = data.token;
  if (!kakaoAccessToken) {
    throw new HttpsError("invalid-argument", "카카오 토큰이 없습니다.");
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

exports.sendTodoNotification = onDocumentCreated(
  "todoGroups/{todoId}",
  async (event) => {
    // v2에서는 event.data가 스냅샷입니다.
    const snap = event.data;
    if (!snap) {
      console.log("데이터가 없습니다.");
      return;
    }

    const todoData = snap.data();

    // 1. 담당자 목록(assigneeIds) 확인
    const assigneeIds = todoData.assigneeIds;
    if (!assigneeIds || assigneeIds.length === 0) {
      console.log("담당자가 없는 투두입니다.");
      return;
    }

    // 투두 제목 가져오기
    const firstTask = todoData.tasks && todoData.tasks[0];
    const title = firstTask ? firstTask.title : "새로운 할 일";
    const taskCount = todoData.tasks ? todoData.tasks.length : 1;

    // 알림 문구
    const notificationBody =
      taskCount > 1
        ? `${title} 외 ${taskCount - 1}건의 할 일이 등록되었어요!`
        : `${title} 할 일이 등록되었어요!`;

    // 2. 담당자들의 FCM 토큰 가져오기
    const tokens = [];

    const userPromises = assigneeIds.map((uid) =>
      db.collection("users").doc(uid).get()
    );

    const userDocs = await Promise.all(userPromises);

    userDocs.forEach((doc) => {
      if (doc.exists) {
        const userData = doc.data();
        if (userData.fcmToken) {
          tokens.push(userData.fcmToken);
        }
      }
    });

    if (tokens.length === 0) {
      console.log("알림을 보낼 토큰이 없습니다.");
      return;
    }

    // 3. 알림 메시지 구성
    const message = {
      notification: {
        title: "Todo가 도착했어요! 🐾",
        body: notificationBody,
      },
      android: {
        notification: {
          sound: "default",
          clickAction: "FLUTTER_NOTIFICATION_CLICK", // 안드로이드 클릭 액션
        },
      },
      tokens: tokens,
    };

    // 4. 전송
    try {
      const response = await admin.messaging().sendEachForMulticast(message);
      console.log(
        "알림 전송 성공:",
        response.successCount,
        "실패:",
        response.failureCount
      );
    } catch (error) {
      console.error("알림 전송 중 에러 발생:", error);
    }
  }
);
