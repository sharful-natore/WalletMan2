import re

with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'r', encoding='utf-8') as f:
    code = f.read()

ps_pattern = r'fun ProfileSetupScreen\([\s\S]*?fun ProfileInfoRow\('

new_ps = '''fun ProfileSetupScreen(
    viewModel: FinanceViewModel,
    language: AppLanguage,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val googleName by viewModel.googleName.collectAsStateWithLifecycle()
    val userAddress by viewModel.userAddress.collectAsStateWithLifecycle()
    val userPhone by viewModel.userPhone.collectAsStateWithLifecycle()
    val userDOB by viewModel.userDOB.collectAsStateWithLifecycle()
    val googlePhotoUrl by viewModel.googlePhotoUrl.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf(googleName ?: "") }
    var address by remember { mutableStateOf(userAddress ?: "") }
    var phone by remember { mutableStateOf(userPhone ?: "") }
    var dob by remember { mutableStateOf(userDOB ?: "") }
    var photoUri by remember { mutableStateOf(googlePhotoUrl ?: "") }

    LaunchedEffect(googleName, userAddress, userPhone, userDOB, googlePhotoUrl) {
        if (name.isBlank() && !googleName.isNullOrBlank()) name = googleName!!
        if (address.isBlank() && !userAddress.isNullOrBlank()) address = userAddress!!
        if (phone.isBlank() && !userPhone.isNullOrBlank()) phone = userPhone!!
        if (dob.isBlank() && !userDOB.isNullOrBlank()) dob = userDOB!!
        if (photoUri.isBlank() && !googlePhotoUrl.isNullOrBlank()) photoUri = googlePhotoUrl!!
    }

    val context = LocalContext.current
    val cropLauncher = rememberLauncherForActivityResult(UCropContract()) { uri ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val destinationUri = Uri.fromFile(java.io.File(context.cacheDir, "profile_setup_crop_${System.currentTimeMillis()}.jpg"))
            cropLauncher.launch(uri to destinationUri)
        }
    }

    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val isPhotoLoading by viewModel.isPhotoLoading.collectAsStateWithLifecycle()
    var isEditMode by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Cover Banner with Avatar overlap
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    // Modern Gradient Cover Background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = if (isDark) listOf(
                                        Color(0xFF1E3A8A),
                                        Color(0xFF3B82F6),
                                        Color(0xFF6366F1)
                                    ) else listOf(
                                        Color(0xFF2563EB),
                                        Color(0xFF3B82F6),
                                        Color(0xFF60A5FA)
                                    )
                                )
                            )
                    ) {
                        // Decorative pattern rings
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .offset(x = (-40).dp, y = (-40).dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        )
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 30.dp, y = 30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        )

                        // Top action bar inside cover photo
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isEditMode) {
                                IconButton(
                                    onClick = { isEditMode = false },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(36.dp))
                            }

                            Text(
                                text = if (isEditMode) (if (language == AppLanguage.BN) "প্রোফাইল সম্পাদনা" else "Edit Profile") else (if (language == AppLanguage.BN) "প্রোফাইল তথ্য" else "Profile Info"),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Avatar overlapping Cover Photo (Bottom Center)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(4.dp, if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC), CircleShape)
                            .background(if (isDark) Color(0xFF1E293B) else Color.White)
                            .then(
                                if (isEditMode) Modifier.clickable { photoLauncher.launch("image/*") } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPhotoLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp,
                                color = FintechBlue
                            )
                        } else if (photoUri.isNotEmpty()) {
                            SubcomposeAsyncImage(
                                model = photoUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(36.dp).padding(6.dp),
                                        strokeWidth = 3.dp,
                                        color = FintechBlue
                                    )
                                }
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = if (isDark) Color.LightGray else Color.Gray,
                                modifier = Modifier.size(60.dp)
                            )
                        }

                        if (isEditMode) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AddAPhoto,
                                    contentDescription = "Change Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                // Main Info Content Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // User Name & Role Badge
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = name.ifBlank { if (language == AppLanguage.BN) "নাম দেওয়া হয়নি" else "No Name Set" },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == AppLanguage.BN) "ফাইনান্স নোট ব্যবহারকারী" else "Finance Note User",
                            fontSize = 12.sp,
                            color = FintechBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(FintechBlue.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                                .border(1.dp, FintechBlue.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    if (!isEditMode) {
                        // Details View Card
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ProfileInfoRow(
                                    icon = Icons.Rounded.Phone,
                                    label = if (language == AppLanguage.BN) "ফোন নম্বর" else "Phone Number",
                                    value = phone.ifBlank { "-" },
                                    isDark = isDark
                                )
                                HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))
                                ProfileInfoRow(
                                    icon = Icons.Rounded.CalendarToday,
                                    label = if (language == AppLanguage.BN) "জন্ম তারিখ" else "Date of Birth",
                                    value = dob.ifBlank { "-" },
                                    isDark = isDark
                                )
                                HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))
                                ProfileInfoRow(
                                    icon = Icons.Rounded.LocationOn,
                                    label = if (language == AppLanguage.BN) "ঠিকানা" else "Address",
                                    value = address.ifBlank { "-" },
                                    isDark = isDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { isEditMode = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.BN) "প্রোফাইল সংশোধন করুন" else "Edit Profile",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        // Edit Mode Fields
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(if (language == AppLanguage.BN) "আপনার নাম" else "Your Name") },
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = FintechBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                focusedLabelColor = FintechBlue,
                                unfocusedLabelColor = Color.Gray
                            )
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(if (language == AppLanguage.BN) "ফোন নম্বর" else "Phone Number") },
                            leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null, tint = FintechBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                focusedLabelColor = FintechBlue,
                                unfocusedLabelColor = Color.Gray
                            )
                        )

                        OutlinedTextField(
                            value = dob,
                            onValueChange = { dob = it },
                            label = { Text(if (language == AppLanguage.BN) "জন্ম তারিখ" else "Date of Birth") },
                            leadingIcon = { Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = FintechBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            placeholder = { Text("DD/MM/YYYY", color = Color.Gray.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                focusedLabelColor = FintechBlue,
                                unfocusedLabelColor = Color.Gray
                            )
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text(if (language == AppLanguage.BN) "ঠিকানা" else "Address") },
                            leadingIcon = { Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = FintechBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                focusedLabelColor = FintechBlue,
                                unfocusedLabelColor = Color.Gray
                            )
                        )

                        if (error != null) {
                            Text(text = error!!, color = Color.Red, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isEditMode = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BN) "বাতিল" else "Cancel",
                                    color = if (isDark) Color.White else Color.Black
                                )
                            }
                            Button(
                                onClick = {
                                    isLoading = true
                                    viewModel.updateUserProfile(
                                        name, address, phone, dob, photoUri,
                                        onSuccess = {
                                            isLoading = false
                                            isEditMode = false
                                        },
                                        onError = { err ->
                                            isLoading = false
                                            error = err
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(
                                        text = if (language == AppLanguage.BN) "সংরক্ষণ করুন" else "Save",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun ProfileInfoRow('''

code = re.sub(ps_pattern, new_ps, code, count=1)

with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'w', encoding='utf-8') as f:
    f.write(code)

print('ProfileSetupScreen successfully updated!')
