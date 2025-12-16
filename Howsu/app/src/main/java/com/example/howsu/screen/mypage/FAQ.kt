package com.example.howsu.screen.mypage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton

data class FAQ(
    val id: Int,
    val title: String,
    val tag: String,
    val content: String
)

val sampleFAQs = listOf(  // sample data
    FAQ(
        id = 1,
        title = "가족 구성원은 어떻게 초대하고 내보내나요?",
        tag = "이용 방법",
        content = """
            가족 구성원 초대에는 2가지 방법이 존재합니다.
            
            1. 마이페이지에서 ‘가족 초대하기’를 통해 QR로 초대를 하는 방식
            2. 아이디를 이용해 초대하는 방식
            
            원하는 방식을 통해 가족을 초대해주시면 됩니다.
            
            가족 구성원을 내보내고 싶을 경우에는
            ~~~~
            """.trimIndent()
    ),
    FAQ(
        id = 2,
        title = "비밀번호를 잊어버렸어요. 어떻게 재설정하나요?",
        tag = "계정",
        content = "비밀번호 재설정 방법 상세 설명"
    ),
    FAQ(
        id = 3,
        title = "반려동물 프로필은 어떻게 추가하나요?",
        tag = "이용 방법",
        content = "반려동물 프로필 추가 방법 상세 설명"
    ),
    FAQ(
        id = 4,
        title = "앱 사용 중 오류가 발생했어요.",
        tag = "기타",
        content = "오류 발생 시 대처 방법 상세 설명"
    ),
)

// 전체 태그 목록 (중복 제거 + "전체 질문" 추가)
val allTags = listOf("전체 질문") + sampleFAQs.map { it.tag }.distinct()


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(
    navController: NavHostController,
){
    // 현재 선택된 태그 상태 (초기값: "전체 질문")
    var selectedTag by remember { mutableStateOf("전체 질문") }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = "자주 묻는 질문",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                ) },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack() }){
                        Icon(Icons.Filled.ArrowBack, contentDescription = "되돌아가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.padding(10.dp)
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
    ){ paddingValues ->

        // 필터링된 질문 목록
        val filteredFAQs = remember(selectedTag) {
            if (selectedTag == "전체 질문") {
                sampleFAQs
            } else {
                sampleFAQs.filter { it.tag == selectedTag }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            // 수평 패딩은 ExpandableFAQItem에서 처리하여 LazyRow가 꽉 차도록 함
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // LazyColumn의 첫 번째 item: 태그 필터 버튼 Row
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    TagFilterRow(
                        tags = allTags,
                        selectedTag = selectedTag,
                        onTagSelected = { tag -> selectedTag = tag }
                    )
                    Spacer(modifier = Modifier.height(24.dp)) // 태그와 목록 사이 간격
                }
            }

            // 필터링된 질문 목록 표시
            items(filteredFAQs) { faq ->
                ExpandableFAQItem(faq = faq)
            }

            // 질문이 없을 경우 (필터링 결과가 없는 경우)
            if (filteredFAQs.isEmpty()) {
                item {
                    Text(
                        text = "선택하신 태그에 해당하는 질문이 없습니다.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 이미지와 같이 태그 필터 버튼들을 가로로 나열하는 컴포넌트
 */
@Composable
fun TagFilterRow(
    tags: List<String>,
    selectedTag: String,
    onTagSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tags) { tag ->
            TagFilterButton(
                tag = tag,
                isSelected = tag == selectedTag,
                onClick = { onTagSelected(tag) }
            )
        }
    }
}

// 태그 필터 버튼
@Composable
fun TagFilterButton(
    tag: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = tag,
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}


@Composable
fun ExpandableFAQItem(faq: FAQ) {
    var isExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{ isExpanded = !isExpanded }
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ){
            Column(modifier = Modifier.weight(1f)){
                // 태그를 제목 위에 작은 글씨로 표시
                Text(
                    text = faq.tag,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.padding(1.dp))
                Text(
                    text = faq.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            val icon = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
            Icon(
                imageVector = icon,
                contentDescription = if (isExpanded) "닫기" else "펼치기",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        if(isExpanded){
            Spacer(modifier = Modifier.padding(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = faq.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
    Divider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Preview(showBackground = true)
@Composable
fun FAQScreenPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        FAQScreen(
            navController = navController
        )
    }
}