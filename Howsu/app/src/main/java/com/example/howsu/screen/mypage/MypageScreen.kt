package com.example.howsu.screen.mypage

// **[추가된 import]**
// **[추가된 import]**
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton


// ----------------------------------------------------
// (Profile Composable은 변경 없음)
// ----------------------------------------------------
@Composable
fun Profile(
    profileImageUrl: String? = null,
    username: String,
    email: String,
    onEditClick: () -> Unit,
){
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(15.dp)
            ),
        shape = MaterialTheme.shapes.medium,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = Color.LightGray,
                        shape = CircleShape
                    )
            ){
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "프로필 이미지",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,

                    )
                if (profileImageUrl.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "기본 프로필",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Create, contentDescription = "내 정보 수정")
            }
        }
    }
}

// ----------------------------------------------------
// mypage 전체 화면
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MypageScreen(
    navController: NavHostController,
    viewModel: MypageViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
){
    // 1. **[수정]** 생명주기 관찰자를 통해 화면 복귀 시 데이터 로드
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        // LifecycleEventObserver 인스턴스 생성
        val observer = LifecycleEventObserver { _, event ->
            // ON_RESUME 이벤트 -> Fragment/Activity가 다시 포그라운드로 올 때 발생
            if (event == Lifecycle.Event.ON_RESUME) {
                // 이 시점에 뷰모델의 데이터 로드 함수를 호출하여 최신 데이터를 가져옴
                viewModel.loadMyInfo()
                Log.d("MypageScreen", "ON_RESUME: Calling viewModel.loadUserProfile()")
            }
        }

        // 관찰자 등록
        lifecycleOwner.lifecycle.addObserver(observer)

        // Composable이 화면에서 제거될 때 관찰자 해제
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 2. 뷰모델 상태 관찰
    val myFamilyName by viewModel.familyName.collectAsState()
    val myFamilyId by viewModel.familyId.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val myProfileUrl by viewModel.myProfileUrl.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "마이페이지",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("profile")}) {
                        Icon(Icons.Filled.Settings, contentDescription = "설정")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {MyBottomNavigationBar(navController = navController)},
        floatingActionButton = {
            MyFloatingActionButton(
                onTodoClick = {
                    navController.navigate("create_todo")
                },
                onScheduleClick = {
                    navController.navigate("create_schedule")
                },
                onFeedCreateClick = {
                    navController.navigate("create_feed")
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Profile(
                username = userName,
                email = userEmail,
                profileImageUrl = myProfileUrl,
                onEditClick = { navController.navigate("edit_profile") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column {
                ContentItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "반려동물 추가") },
                    text = "반려동물 추가하기",
                    onClick = { navController.navigate("register_pet") }
                )
                Divider(modifier = Modifier.padding(horizontal = 25.dp))

                ContentItem(
                    icon = { Icon(Icons.Default.People, contentDescription = "가족 정보") },
                    text = "가족 정보 확인하기",
                    onClick = { navController.navigate("family_info") }
                )
                Divider(modifier = Modifier.padding(horizontal = 25.dp))

                ContentItem(
                    icon = {
                        Icon(
                            Icons.Default.GroupAdd,
                            contentDescription = "가족 초대"
                        )
                    },
                    text = "가족 초대하기",
                    onClick = {
                        // URL 인코딩 처리
                        val encodedUrl = if (myProfileUrl != null) {
                            java.net.URLEncoder.encode(myProfileUrl, java.nio.charset.StandardCharsets.UTF_8.toString())
                        } else {
                            "null"
                        }

                        navController.navigate("invite_family/$myFamilyName/$myFamilyId?profileUrl=$encodedUrl&isFromMypage=true")
                    }
                )
                Divider(modifier = Modifier.padding(horizontal = 25.dp))

                ContentItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "공지사항") },
                    text = "공지사항",
                    onClick = { navController.navigate("notice") }
                )
                Divider(modifier = Modifier.padding(horizontal = 25.dp))

                ContentItem(
                    icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = "자주 묻는 질문") },
                    text = "자주 묻는 질문",
                    onClick = { navController.navigate("faq") }
                )
                Divider(modifier = Modifier.padding(horizontal = 25.dp))
            }
        }
    }
}


// ----------------------------------------------------
// (ContentItem Composable은 변경 없음)
// ----------------------------------------------------
@Composable
fun ContentItem(
    icon: @Composable () -> Unit,
    text: String,
    onClick: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable (onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        icon()
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "세부 내용으로 이동",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}