// /screen/schedule/ScheduleDetailScreen.kt (★ 파일 전체 교체)

package com.example.howsu.screen.schedule

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.R
import com.example.howsu.ui.theme.HowsuTheme

/**
 * 일정 자세히 보기 화면 (디자인 3번) - ★ 진짜 최종 수정본 ★
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailScreen(
    navController: NavHostController,
    scheduleId: String?, // "temp_id" 또는 실제 Firestore ID
    viewModel: ScheduleViewModel = viewModel()
) {
    // 예시 데이터 (ViewModel에서 가져와야 함)
    val scheduleTitle = "병원 방문" // (디자인 1번의 "뭐야"에 해당)
    val scheduleMemo = "자몽이 3차 예방접종" // (디자인 1번의 "안녕하세요----"에 해당)
    val pet1 = "자몽"
    val pet2 = "레몬"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DetailTopBar(
                onBackClick = { navController.popBackStack() },
                onEditClick = {
                    navController.navigate("edit_schedule/$scheduleId")
                }
            )
        },
        bottomBar = {
            DeleteScheduleBottomButton(
                onDeleteClick = { // (이름을 onDeleteClick으로 변경하는 것을 추천)
                    if (scheduleId != null) {
                        // ★ 4. ViewModel의 삭제 함수 호출
                        viewModel.deleteSchedule(scheduleId) {
                            // ★ 5. 삭제 성공 시 (onComplete), 뒤로 가기
                            navController.popBackStack()
                        }
                    } else {
                        Log.e("ScheduleDetailScreen", "scheduleId가 null이라 삭제할 수 없습니다.")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp) // 항목 간 간격
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 제목
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp) // ★ 제목과 줄 사이 간격
            ) {
                // 1. 제목 + 아이콘 행
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = scheduleTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.weight(1f)) // ★ 아이콘들을 오른쪽으로 밀어냄

                    Icon(
                        painter = painterResource(id = R.drawable.mood),
                        contentDescription = "기분",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp)) // ★ 아이콘 사이 간격
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4285F4)) // 파란색 원
                    )
                }

                // 2. 디자인에 있는 구분선
                Divider(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    thickness = 1.dp
                )
            }

            // --- ★ 2. "하루 종일" ---
            DetailInfoRow(
                icon = Icons.Default.Schedule,
                title = "하루 종일"
            ) {
                // Row의 맨 끝에 배치될 컨텐츠
                Switch(
                    checked = false,
                    onCheckedChange = null,
                    enabled = false
                )
            }

            // --- ★ 3. "날짜" (디자인 1번처럼 아이콘 없고 들여쓰기) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { /* TODO: 시작 시간 피커 */ }
                ) {
                    Text("11월 1일 (토)", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("오전 08:00", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "에서",
                    modifier = Modifier.size(20.dp)
                )

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { /* TODO: 종료 시간 피커 */ }
                ) {
                    Text("11월 1일 (토)", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("오전 09:00", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }


            // --- ★ 4. "반복" (디자인 1번처럼 제목 = "매일") ---
            DetailInfoRow(
                icon = Icons.Default.Refresh,
                title = "매일" // 값이 제목이 됨
            ) {
                // 오른쪽 컨텐츠 없음
            }

            // --- ★ 5. "알림" (디자인 1번처럼 제목 = "1일 전...") ---
            DetailInfoRow(
                icon = Icons.Default.Notifications,
                title = "1일 전 오후 5:00" // 값이 제목이 됨
            ) {
                // 오른쪽 컨텐츠 없음
            }

            // --- ★ 6. "한 줄 메모" ---
            DetailInfoColumn(
                icon = Icons.Default.Comment,
                title = "한 줄 메모"
            ) {
                OutlinedTextField(
                    value = scheduleMemo,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("안녕하세요----") } // 디자인 1의 placeholder
                )
            }

            // --- ★ 7. "반려동물" ---
            DetailInfoColumn(
                icon = Icons.Default.Pets,
                title = "반려동물"
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PetChip(name = pet1)
                    PetChip(name = pet2)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 상단 바 (뒤로가기, 수정)
 */
@Composable
private fun DetailTopBar(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding() // 상태바 패딩
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(40.dp)
    ) {
        // 뒤로가기
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(39.dp)
                .align(Alignment.CenterStart)
                .border(BorderStroke(0.1.dp, Color.LightGray), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로가기",
                modifier = Modifier.size(24.dp)
            )
        }

        // 중앙 타이틀
        Text(
            text = "일정 자세히 보기",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center)
        )

        // 수정 버튼
        IconButton(
            onClick = onEditClick,
            modifier = Modifier
                .size(39.dp)
                .align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "수정하기",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 정보 행 템플릿 (컨텐츠가 오른쪽에 오는 경우)
 */
@Composable
private fun DetailInfoRow(
    icon: ImageVector,
    title: String,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.weight(1f)) // 내용(content)을 오른쪽 끝으로 밀어냄

        content()
    }
}

/**
 * 정보 열 템플릿 (컨텐츠가 아래에 오는 경우)
 */
@Composable
private fun DetailInfoColumn(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp) // 제목과 컨텐츠 간격
    ) {
        // 제목 행
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        // 컨텐츠 (아이콘 너비(22) + 간격(12) = 34.dp 만큼 들여쓰기)
        Box(modifier = Modifier.padding(start = 34.dp)) {
            content()
        }
    }
}


/**
 * 반려동물 칩 (디자인과 동일하게 수정)
 */
@Composable
private fun PetChip(name: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant, // 밝은 회색
        modifier = Modifier.clickable { /* 펫 정보 보기? */ }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background), // TODO: 펫 이미지
                contentDescription = name,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
            )
            Text(name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        }
    }
}

@Composable
private fun DeleteScheduleBottomButton(onDeleteClick: () -> Unit) { // 'onCreateClick' -> 'onDeleteClick'
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Button(
            onClick = onDeleteClick, // ★ 7. 파라미터 연결
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black, // (삭제 버튼이니 빨간색 Color(0xFFEA4335) 추천)
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("삭제하기", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF) // 흰 배경
@Composable
fun ScheduleDetailScreenPreview() {
    HowsuTheme {
        ScheduleDetailScreen(rememberNavController(), "123")
    }
}