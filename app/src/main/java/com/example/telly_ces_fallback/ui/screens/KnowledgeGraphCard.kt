package com.example.telly_ces_fallback.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KnowledgeGraphCard() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.Black) // Background for the screen
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)), // Dark Card Background
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title with Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning, // Replace with custom icon if available
                        contentDescription = "Boss Tips Icon",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Bayle the Dread Boss Tips",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Description
                Text(
                    text = "Dragons are generally weak to physical damage, particularly from strikes, and are also vulnerable to elemental damage like lightning and fire.",
                    fontSize = 14.sp,
                    color = Color.White
                )

                // Tip 1
                Text(
                    text = "1. Strike Damage:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Weapons that deal strike damage (such as hammers and clubs) are effective against dragons.",
                    fontSize = 14.sp,
                    color = Color.White
                )

                // Tip 2
                Text(
                    text = "2. Lightning Damage:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Dragons have a notable weakness to lightning, so using weapons or incantations that deal lightning damage can be highly effective.",
                    fontSize = 14.sp,
                    color = Color.White
                )

                // Tip 3
                Text(
                    text = "3. Fire Damage:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "While some dragons breathe fire, they are still susceptible to fire damage. Incantations or weapons with fire enhancements can be useful.",
                    fontSize = 14.sp,
                    color = Color.White
                )

                // Conclusion Text
                Text(
                    text = "Using these types of damage will give you an advantage in battles against dragons in Elden Ring.",
                    fontSize = 14.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewBossTipsCard() {
    KnowledgeGraphCard()
}