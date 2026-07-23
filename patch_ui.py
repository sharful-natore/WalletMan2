with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'r') as f:
    content = f.read()

# Add bottom padding to SavingsGoalDetailOverlay LazyColumn
content = content.replace(
'''                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {''',
'''                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {'''
)

# Add bottom padding to PersonDetailOverlay LazyColumn
content = content.replace(
'''                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    grouped.forEach { (date, txs) ->''',
'''                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    grouped.forEach { (date, txs) ->'''
)

# Add shadow to Navbar Add button
content = content.replace(
'''                    Box(
                        modifier = Modifier
                            .padding(bottom = 8.dp + navBarPadding) // Lowered to nestle perfectly in the notch
                            .size(64.dp)
                            .clip(CircleShape)''',
'''                    Box(
                        modifier = Modifier
                            .padding(bottom = 8.dp + navBarPadding) // Lowered to nestle perfectly in the notch
                            .shadow(elevation = 8.dp, shape = CircleShape, spotColor = Color(0xFF6F7BF7), ambientColor = Color(0xFF38BDF8))
                            .size(64.dp)
                            .clip(CircleShape)'''
)

with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'w') as f:
    f.write(content)
