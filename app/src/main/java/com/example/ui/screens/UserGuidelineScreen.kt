package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.AppLanguage

data class GuidelineItemData(
    val id: Int,
    val category: String,
    val titleBn: String,
    val titleEn: String,
    val badgeBn: String,
    val badgeEn: String,
    val badgeColor: Color,
    val icon: ImageVector,
    val stepsBn: List<String>,
    val stepsEn: List<String>,
    val proTipBn: String,
    val proTipEn: String,
    val illustrationType: String
)

@Composable
fun UserGuidelineScreen(
    language: AppLanguage,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var expandedItemIds by remember { mutableStateOf(setOf(1)) } // Default first item expanded

    val isBn = language == AppLanguage.BN

    val categories = listOf(
        Pair("all", if (isBn) "সকল ফিচার" else "All Features"),
        Pair("income", if (isBn) "আয়-ব্যয়" else "Income & Expense"),
        Pair("savings", if (isBn) "সঞ্চয় ও বাজেট" else "Savings & Budget"),
        Pair("debt", if (isBn) "দেনা-পাওনা" else "Debt Ledger"),
        Pair("trend", if (isBn) "রিপোর্ট ও ট্রেন্ড" else "Reports & Trends"),
        Pair("auto", if (isBn) "অটো এন্ট্রি ও ড্রাফট" else "Auto SMS & Drafts"),
        Pair("workspace", if (isBn) "ওয়ার্কস্পেস ও ব্যাকআপ" else "Workspace & Backup")
    )

    val guidelinesList = listOf(
        GuidelineItemData(
            id = 1,
            category = "income",
            titleBn = "আয় ও ব্যয় ট্র্যাকিং করার নিয়ম",
            titleEn = "How to Track Income & Expenses",
            badgeBn = "মূল ফিচার",
            badgeEn = "Core Feature",
            badgeColor = FintechBlue,
            icon = Icons.Rounded.ReceiptLong,
            stepsBn = listOf(
                "হোম স্ক্রিনের নিচে ডান কোণে (+) প্লাস বাটন চাপুন।",
                "টাকার পরিমাণ এবং লেনদেনের ধরন (আয় বা ব্যয়) নির্বাচন করুন।",
                "ক্যাটাগরি (যেমন: বেতন, কেনাকাটা, খাবার, যাতায়াত) এবং পেমেন্ট ওয়ালেট বেছে নিন।",
                "ব্যস্ত সময় থাকলে ভয়েস ইনপুট দিয়ে কথা বলেই এন্ট্রি রেকর্ড করুন অথবা ড্রাফটে সেভ রাখুন।",
                "সেভ চাপলেই রিয়েল-টাইমে মোট ব্যালেন্স ও চার্ট আপডেট হবে।"
            ),
            stepsEn = listOf(
                "Tap the (+) plus button at the bottom right of the Home screen.",
                "Enter amount and select transaction type (Income or Expense).",
                "Choose category (e.g. Salary, Shopping, Food, Transport) and payment wallet.",
                "Use voice input for quick entry when busy or save as a draft for later.",
                "Tap Save to instantly update overall balance and category charts."
            ),
            proTipBn = "পরামর্শ: দ্রুততম এন্ট্রির জন্য ১-ট্যাপ প্রিয় ক্যাটাগরি পিন করে রাখুন।",
            proTipEn = "Pro Tip: Pin your favorite categories for instant 1-tap logging.",
            illustrationType = "income"
        ),
        GuidelineItemData(
            id = 2,
            category = "savings",
            titleBn = "সঞ্চয় লক্ষ্য (Savings Vault) তৈরি ও জমা-উত্তোলন",
            titleEn = "Create Savings Vaults & Manage Deposits",
            badgeBn = "সঞ্চয়",
            badgeEn = "Savings",
            badgeColor = Color(0xFF10B981),
            icon = Icons.Rounded.AccountBalance, // Bank Icon used instead of pig icon!
            stepsBn = listOf(
                "হোম স্ক্রিন বা নেভিগেশন থেকে 'সঞ্চয়' (Savings) অপশনে প্রবেশ করুন।",
                "'নতুন সঞ্চয় লক্ষ্য' বাটনে ট্যাপ করে লক্ষ্যের নাম (যেমন: ডিপিএস, বাইক বা ইমার্জেন্সি ফান্ড) দিন।",
                "টার্গেট টাকার পরিমাণ ও মেয়াদ নির্ধারণ করে নিশ্চিত করুন।",
                "প্রতিবার সঞ্চয় হলে 'টাকা জমা' (Deposit) চাপুন এবং জমা আপডেট করুন।",
                "প্রয়োজনে জমাকৃত টাকা থেকে 'উত্তোলন' (Withdrawal) এর হিসেবও আলাদাভাবে ট্র্যাক রাখা যাবে।"
            ),
            stepsEn = listOf(
                "Go to the 'Savings' section from the home screen or menu.",
                "Tap 'New Savings Goal' and set goal title (e.g., DPS, Emergency Fund) and target amount.",
                "Define target completion amount and deadline.",
                "Tap 'Deposit' whenever you save money to boost progress bar.",
                "Record withdrawals anytime to maintain precise remaining vault balances."
            ),
            proTipBn = "পরামর্শ: সঞ্চয় লক্ষ্য ১০০% পূরণ হলে অ্যাপ চমৎকার উদযাপনী নোটিফিকেশন পাঠাবে!",
            proTipEn = "Pro Tip: Get automated celebration alerts once goals reach 100% completion!",
            illustrationType = "savings"
        ),
        GuidelineItemData(
            id = 3,
            category = "savings",
            titleBn = "স্মার্ট বাজেট সীমা ও ওভার-স্পেন্ডিং অ্যালার্ট",
            titleEn = "Set Category Budgets & Prevent Over-spending",
            badgeBn = "বাজেট",
            badgeEn = "Budget",
            badgeColor = Color(0xFFF59E0B),
            icon = Icons.Rounded.PieChart,
            stepsBn = listOf(
                "'বাজেট' ট্যাবে গিয়ে আপনার মাসিক খরচের সর্বোচ্চ সীমা নির্ধারণ করুন।",
                "নির্দিষ্ট ক্যাটাগরির (যেমন: বাইরের খাবার বা বিনোদন) জন্য পৃথক বাজেট ক্যাপ সেট করুন।",
                "খরচ ৮০% ও ১০০% অতিক্রম করলে অ্যাপ স্বয়ংক্রিয় সতর্ক বার্তা পাঠাবে।",
                "বাজেট অগ্রগতি বার দেখে সহজেই বুঝতে পারবেন চলতি মাসে কত টাকা খরচ করা নিরাপদ।"
            ),
            stepsEn = listOf(
                "Go to 'Budget' tab and set your monthly maximum spending cap.",
                "Set individual budget limits for specific categories like Dining or Entertainment.",
                "Receive intelligent alerts when reaching 80% and 100% of your limit.",
                "Use the budget progress bar to monitor daily remaining spending power."
            ),
            proTipBn = "পরামর্শ: ক্যাটাগরি বাজেট ব্যবহারে অপচয় রোধ হয় এবং আপনার সঞ্চয় বহুগুণ বাড়ে।",
            proTipEn = "Pro Tip: Category budgets eliminate unnecessary spending habits.",
            illustrationType = "budget"
        ),
        GuidelineItemData(
            id = 4,
            category = "debt",
            titleBn = "দেনা-পাওনা ও ধার-দেনার ডিজিটাল খাতা",
            titleEn = "Digital Debt & Receivable Ledger System",
            badgeBn = "দেনা-পাওনা",
            badgeEn = "Debts",
            badgeColor = Color(0xFF8B5CF6),
            icon = Icons.Rounded.Handshake,
            stepsBn = listOf(
                "'দেনা-পাওনা' ট্যাবে গিয়ে নতুন ব্যক্তি বা বন্ধুর নাম যুক্ত করুন।",
                "কাউকে টাকা ধার দিলে 'পাওনা' (Lend) এবং ধার নিলে 'দেনা' (Borrow) হিসেবে ইনপুট দিন।",
                "আংশিক বা সম্পূর্ণ টাকা ফেরত পেলে 'পরিশোধ' (Repay Paid/Received) বাটনে ক্লিক করে ব্যালেন্স কমিয়ে আনুন।",
                "ব্যক্তির নামের ওপর ক্লিক করে তার সাথে সম্পন্ন হওয়া সমস্ত লেনদেনের হিস্ট্রি দেখুন।"
            ),
            stepsEn = listOf(
                "Navigate to 'Debt Ledger' tab and add a person contact.",
                "Record money given as 'Receivable (Lend)' and money taken as 'Payable (Borrow)'.",
                "Tap 'Repay' when partial or full repayment is completed to update balance.",
                "Click on any contact name to view their complete transaction statement history."
            ),
            proTipBn = "পরামর্শ: টাকা আদায়ের তাগাদা দিতে অ্যাপ থেকেই সরাসরি SMS বা নোটিফিকেশন রিমাইন্ডার পাঠানো যায়।",
            proTipEn = "Pro Tip: Send automated payment reminder notifications directly from the app.",
            illustrationType = "debt"
        ),
        GuidelineItemData(
            id = 5,
            category = "trend",
            titleBn = "আর্থিক অগ্রগতি, পাই-চার্ট ও এইচডি পিডিএফ রিপোর্ট",
            titleEn = "Financial Trends, Charts & PDF Exports",
            badgeBn = "রিপোর্ট",
            badgeEn = "Analytics",
            badgeColor = Color(0xFF06B6D4),
            icon = Icons.Rounded.TrendingUp,
            stepsBn = listOf(
                "'রিপোর্ট' ট্যাবে গিয়ে মাসিক ও বাৎসরিক আয়-ব্যয়ের পাই-চার্ট ও বার-গ্রাফ দেখুন।",
                "নেট-ওয়ার্থ (মোট সম্পত্তি মাইনাস মোট দেনা) এর ধারাবাহিক অগ্রগতি লক্ষ্য করুন।",
                "উপরের ডানপাশের 'PDF Export' আইকনে চাপ দিয়েHD পিডিএফ স্টেটমেন্ট ডাউনলোড বা প্রিন্ট করুন।",
                "নির্দিষ্ট সময়কাল (যেমন: চলতি মাস, গত মাস বা কাস্টম তারিখ) ফিল্টার করে হিসাব দেখতে পারেন।"
            ),
            stepsEn = listOf(
                "Visit 'Analytics' tab to view monthly & yearly pie charts and bar graphs.",
                "Track your Net-Worth growth trajectory over time.",
                "Tap the top right 'PDF Export' icon to download clean printable statements.",
                "Filter financial data by custom date range or category breakdown."
            ),
            proTipBn = "পরামর্শ: হিসাব বিবরণী পিডিএফ এ এক্সপোর্ট করে প্রিয়জন বা হিসাবরক্ষকের সাথে শেয়ার করুন।",
            proTipEn = "Pro Tip: Share generated PDF financial statements with your accountant easily.",
            illustrationType = "trend"
        ),
        GuidelineItemData(
            id = 6,
            category = "auto",
            titleBn = "ব্যাংক/বিকাশ SMS থেকে অটো এন্ট্রি ও ড্রাফট লেনদেন",
            titleEn = "Auto SMS Transaction Detection & Drafts",
            badgeBn = "স্মার্ট",
            badgeEn = "Smart",
            badgeColor = Color(0xFFEC4899),
            icon = Icons.Rounded.Autorenew,
            stepsBn = listOf(
                "প্রোফাইল মেনু থেকে 'স্বয়ংক্রিয় এন্ট্রি (অটো এন্ট্রি)' এনাবল করে রাখুন।",
                "ব্যাংক বা মোবাইল ব্যাংকিং (bKash/Nagad/Rocket) থেকে SMS আসলে অ্যাপ স্বয়ংক্রিয়ভাবে পরিমাণ ও ক্যাটাগরি শনাক্ত করবে।",
                "নোটিফিকেশনে ১-ক্লিকে ট্যাপ করলেই সাথে সাথে লেনদেন যুক্ত হয়ে যাবে।",
                "কোনো লেনদেন পরে সময় নিয়ে এডিট করতে চাইলে তা 'ড্রাফট' হিসেবে সেভ রাখুন।"
            ),
            stepsEn = listOf(
                "Enable 'Auto Entry System' from profile options menu.",
                "When receiving banking SMS (bKash, Nagad, Bank), the app automatically detects amount & type.",
                "Tap 1-click confirm on notification to save automatically.",
                "Save rushed entries into 'Drafts' and finalize details when convenient."
            ),
            proTipBn = "পরামর্শ: অটো এন্ট্রি আপনাকে ব্যস্ততার মাঝেও কোনো হিসাব ভুলে যাওয়া থেকে রক্ষা করে।",
            proTipEn = "Pro Tip: Auto SMS capture guarantees zero forgotten expenses while traveling.",
            illustrationType = "auto"
        ),
        GuidelineItemData(
            id = 7,
            category = "workspace",
            titleBn = "মাল্টি-ওয়ার্কস্পেস, ক্লাউড ব্যাকআপ ও বায়োমেট্রিক সিকিউরিটি",
            titleEn = "Multi-Workspace, Cloud Backup & Security",
            badgeBn = "সিকিউরিটি",
            badgeEn = "Security",
            badgeColor = FintechBlue,
            icon = Icons.Rounded.Workspaces,
            stepsBn = listOf(
                "প্রোফাইল মেনুতে গিয়ে 'ওয়ার্কস্পেস' অপশন থেকে ব্যক্তিগত, পরিবার বা ব্যবসার জন্য আলাদা ওয়ার্কস্পেস তৈরি করুন।",
                "'ব্যাকআপ ও রিস্টোর' এ গিয়ে ১-ক্লিকে গুগল ড্রাইভে ডাটা সুরক্ষিত রাখুন।",
                "নতুন ফোনে অ্যাপ ইনস্টল করলে ড্রাইভে থাকা ব্যাকআপ ফাইল সিলেক্ট করে সব হিসাব রিস্টোর করুন।",
                "গোপনীয়তা বজায় রাখতে ফিঙ্গারপ্রিন্ট বা অ্যাপ সিকিউরিটি পিন লক অন করুন।"
            ),
            stepsEn = listOf(
                "Go to Profile Menu -> 'Workspace' to switch between Personal, Family & Business wallets.",
                "Use 'Backup & Restore' to save 1-click encrypted backups to your Google Drive.",
                "Restore complete history instantly when switching or upgrading to a new smartphone.",
                "Enable Biometric Fingerprint or PIN code lock for absolute financial privacy."
            ),
            proTipBn = "পরামর্শ: গুগল ড্রাইভে ব্যাকআপ চালু রাখলে ফোন হারিয়ে গেলেও তথ্য নিরাপদ থাকে।",
            proTipEn = "Pro Tip: Google Drive sync ensures your data is safe even if your device is replaced.",
            illustrationType = "workspace"
        )
    )

    val filteredList = guidelinesList.filter { item ->
        val matchesCategory = selectedCategory == "all" || item.category == selectedCategory
        val titleText = if (isBn) item.titleBn else item.titleEn
        val stepsText = (if (isBn) item.stepsBn else item.stepsEn).joinToString(" ")
        val matchesSearch = searchQuery.isBlank() || titleText.contains(searchQuery, ignoreCase = true) || stepsText.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Navigation Bar
                Surface(
                    color = if (isDark) Color(0xFF1E293B) else Color.White,
                    shadowElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowBack,
                                        contentDescription = "Back",
                                        tint = if (isDark) Color.White else Color(0xFF0F172A)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (isBn) "ইউজার গাইডলাইন" else "User Guidelines",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = if (isBn) "ফাইন্যান্স নোট ব্যবহারের সহজ নির্দেশিকা" else "Complete usage guide for Finance Note",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            // Help Book Badge
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(FintechBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MenuBook,
                                    contentDescription = null,
                                    tint = FintechBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Search Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = if (isBn) "ফিচার বা গাইডলাইন খুঁজুন..." else "Search feature guide...",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = FintechBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Clear",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FintechBlue,
                                unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                focusedContainerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                                unfocusedContainerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Chips Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { (catId, label) ->
                                val isSelected = selectedCategory == catId
                                Surface(
                                    onClick = { selectedCategory = catId },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) FintechBlue else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) FintechBlue else Color.Transparent
                                    )
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else (if (isDark) Color.LightGray else Color.DarkGray),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Guidelines Accordion List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBn) "ফিচার নির্দেশিকাসমূহ (${filteredList.size})" else "Feature Manuals (${filteredList.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.LightGray else Color.DarkGray
                            )

                            TextButton(
                                onClick = {
                                    expandedItemIds = if (expandedItemIds.size == guidelinesList.size) {
                                        emptySet()
                                    } else {
                                        guidelinesList.map { it.id }.toSet()
                                    }
                                }
                            ) {
                                Text(
                                    text = if (expandedItemIds.size == guidelinesList.size) {
                                        (if (isBn) "সব কলাপ্স করুন" else "Collapse All")
                                    } else {
                                        (if (isBn) "সব এক্সপান্ড করুন" else "Expand All")
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FintechBlue
                                )
                            }
                        }
                    }

                    items(filteredList, key = { it.id }) { item ->
                        val isExpanded = expandedItemIds.contains(item.id)

                        GuidelineAccordionCard(
                            item = item,
                            isExpanded = isExpanded,
                            isBn = isBn,
                            isDark = isDark,
                            onToggle = {
                                expandedItemIds = if (isExpanded) {
                                    expandedItemIds - item.id
                                } else {
                                    expandedItemIds + item.id
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GuidelineAccordionCard(
    item: GuidelineItemData,
    isExpanded: Boolean,
    isBn: Boolean,
    isDark: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (isExpanded) item.badgeColor.copy(alpha = 0.5f) else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Row (Clickable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(item.badgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = item.badgeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isBn) item.titleBn else item.titleEn,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = item.badgeColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (isBn) item.badgeBn else item.badgeEn,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = item.badgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = if (isDark) Color.LightGray else Color.DarkGray
                    )
                }
            }

            // Expanded Details Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f))

                    // Steps Header
                    Text(
                        text = if (isBn) "ব্যবহারের ধাপসমূহ:" else "Step-by-Step Instructions:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = FintechBlue
                    )

                    // Step Items
                    val steps = if (isBn) item.stepsBn else item.stepsEn
                    steps.forEachIndexed { index, step ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(item.badgeColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = item.badgeColor
                                )
                            }

                            Text(
                                text = step,
                                fontSize = 13.sp,
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Visual Illustration Card for each feature
                    Surface(
                        color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isBn) "ভিউ ও ইন্টারফেস ডেমো:" else "Visual Interface Demo:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )

                            when (item.illustrationType) {
                                "income" -> DemoIncomeExpense(isBn, isDark)
                                "savings" -> DemoSavings(isBn, isDark)
                                "budget" -> DemoBudget(isBn, isDark)
                                "debt" -> DemoDebt(isBn, isDark)
                                "trend" -> DemoTrend(isBn, isDark)
                                "auto" -> DemoAutoSMS(isBn, isDark)
                                else -> DemoWorkspace(isBn, isDark)
                            }
                        }
                    }

                    // Pro Tip Box
                    val proTip = if (isBn) item.proTipBn else item.proTipEn
                    Surface(
                        color = item.badgeColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, item.badgeColor.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = null,
                                tint = item.badgeColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = proTip,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Visual Demos for Accordion Items
@Composable
private fun DemoIncomeExpense(isBn: Boolean, isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = Color(0xFF10B981).copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Rounded.ArrowDownward, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                Column {
                    Text(text = if (isBn) "আয় (বেতন)" else "Income", fontSize = 10.sp, color = Color.Gray)
                    Text(text = "৳৫০,০০০", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }
        Surface(
            color = Color(0xFFEF4444).copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Rounded.ArrowUpward, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                Column {
                    Text(text = if (isBn) "ব্যয় (বাজার)" else "Expense", fontSize = 10.sp, color = Color.Gray)
                    Text(text = "৳১,২০০", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
            }
        }
    }
}

@Composable
private fun DemoSavings(isBn: Boolean, isDark: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Bank Icon used instead of pig icon!
                Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                Text(text = if (isBn) "জরুরি সঞ্চয় তহবিল" else "Emergency Vault", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
            }
            Text(text = "৭০%", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
        }
        LinearProgressIndicator(
            progress = { 0.7f },
            color = Color(0xFF10B981),
            trackColor = Color(0xFF10B981).copy(alpha = 0.2f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}

@Composable
private fun DemoBudget(isBn: Boolean, isDark: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = if (isBn) "মাসিক বাজেট ক্যাপ (৳২৫,০০০)" else "Monthly Cap ($25,000)", fontSize = 11.sp, color = Color.Gray)
            Text(text = if (isBn) "ব্যয়: ৳১৮,০০০ (৭২%)" else "Spent: 72%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FintechBlue)
        }
        LinearProgressIndicator(
            progress = { 0.72f },
            color = FintechBlue,
            trackColor = FintechBlue.copy(alpha = 0.2f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}

@Composable
private fun DemoDebt(isBn: Boolean, isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Rounded.Person, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
            Text(text = if (isBn) "সাকিব হাসান (পাওনা)" else "Sakib (Receivable)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
        }
        Text(text = "৳৩,৫০০", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
    }
}

@Composable
private fun DemoTrend(isBn: Boolean, isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(16.dp))
            Text(text = if (isBn) "নেট-ওয়ার্থ বৃদ্ধি" else "Net Worth Growth", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
        }
        Text(text = "+১৫.৪%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
    }
}

@Composable
private fun DemoAutoSMS(isBn: Boolean, isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Rounded.Message, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
            Text(text = if (isBn) "bKash: ৳৫০০ ক্যাশইন প্রাপ্ত" else "bKash: $500 Cash In Detected", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color.Black)
        }
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFFEC4899).copy(alpha = 0.2f)
        ) {
            Text(text = if (isBn) "১-ট্যাপ পোস্ট" else "1-Tap Post", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}

@Composable
private fun DemoWorkspace(isBn: Boolean, isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Rounded.CloudDone, contentDescription = null, tint = FintechBlue, modifier = Modifier.size(16.dp))
            Text(text = if (isBn) "গুগল ড্রাইভ ব্যাকআপ: সম্পূর্ণ" else "Google Drive Sync: Active", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color.Black)
        }
        Icon(Icons.Rounded.Fingerprint, contentDescription = null, tint = FintechBlue, modifier = Modifier.size(18.dp))
    }
}
