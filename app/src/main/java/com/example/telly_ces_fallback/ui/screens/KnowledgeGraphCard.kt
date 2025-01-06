import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.telly_ces_fallback.R
import com.example.telly_ces_fallback.model.knowledge_graph.KnowledgeGraphResponse
import com.example.telly_ces_fallback.model.knowledge_graph.KnowledgeGraphResult
import com.example.telly_ces_fallback.ui.theme.CurrentText
import com.example.telly_ces_fallback.ui.theme.SurfaceOverlay
import kotlinx.serialization.json.Json

@Composable
fun KnowledgeGraphCard(
    knowledgeGraph: KnowledgeGraphResult,
    modifier: Modifier = Modifier
) {
    Surface (
        shape = MaterialTheme.shapes.large,
        color = SurfaceOverlay,
        tonalElevation = 2.dp,
        shadowElevation = 10.dp,
        modifier = modifier.width(400.dp).wrapContentHeight().padding(2.dp)
    ) {
        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.color_without_glow),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp)
                    )
                    Text(
                        text = knowledgeGraph.name ?: "",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = CurrentText,
                            fontWeight = FontWeight.Medium,
                            fontSize = 17.54.sp
                        )
                    )

                }
                knowledgeGraph.image?.contentUrl?.let { imageUrl ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .align(Alignment.CenterHorizontally)
                    )
                }
                Text(
                    text = knowledgeGraph.description ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CurrentText
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Description Section
                Text(
                    text = knowledgeGraph.detailedDescription?.articleBody ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = CurrentText,
                    lineHeight = 24.sp
                )
            }
        }

}
}

@Preview(showBackground = true)
@Composable
fun AnimalInfoCardPreview() {
    val jsonResponse = """
        {
        "result": {
        "name": "Flying squirrel",
        "description": "A nocturnal rodent with a furry membrane.",
        "image": {
        "contentUrl": "https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcR2caGRBWDrDkpHZNbjM5_jSa_3xVEt8kbiZnuhWlfRW8Jc8Jdt"
        },
        "detailedDescription": {
        "articleBody": "Flying squirrels are a tribe of 50 species of squirrels in the family Sciuridae. Despite their name, they are not in fact capable of full flight in the same way as birds or bats, but they are able to glide from one tree to another with the aid of a patagium, a furred skin membrane that stretches from wrist to ankle."
        },
        "url": "https://en.wikipedia.org/wiki/Flying_squirrel"
        }
        }
    """
    val parsedResult = Json { ignoreUnknownKeys = true }
        .decodeFromString<KnowledgeGraphResponse>(jsonResponse).result
    MaterialTheme {
        KnowledgeGraphCard(
            knowledgeGraph = parsedResult,
            modifier = Modifier.padding(16.dp)
        )
    }
}