package com.joesemper.fishing.ui.composable

import android.accounts.AuthenticatorDescription
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.theme.secondaryFigmaColor

@Composable
fun MyCardNoPadding(content: @Composable () -> Unit) {
    Card(elevation = 4.dp, shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(), content = content)
}

@Composable
fun MyCard(content: @Composable () -> Unit) {
    Card(elevation = 8.dp, modifier = Modifier.fillMaxWidth().padding(4.dp), content = content)
}

@Composable
fun UserProfile(user: User?) {
    user?.let { nutNullUser ->
        Crossfade(nutNullUser, animationSpec = tween(500)) { animatedUser ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberImagePainter(
                        data = animatedUser.userPic,
                        builder = {
                            transformations(CircleCropTransformation())
                            //crossfade(500)
                        }
                    ),
                    contentDescription = stringResource(R.string.fisher),
                    modifier = Modifier.padding(9.dp),
                )
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        animatedUser.userName.split(" ")[0],
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.button.fontSize
                    )
                    Text(
                        "@" + stringResource(R.string.fisher),
                        fontSize = MaterialTheme.typography.caption.fontSize
                    )
                }
            }
        }
    } ?: Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.ic_fisher),
            contentDescription = stringResource(R.string.fisher),
            Modifier.fillMaxHeight().padding(10.dp)
        )
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                stringResource(R.string.fisher),
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.button.fontSize
            )
            Text(
                "@" + stringResource(R.string.fisher),
                fontSize = MaterialTheme.typography.caption.fontSize
            )
        }
    }
}

@Composable
fun PlaceInfo(user: User?, place: UserMapMarker) {
    MyCardNoPadding {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp).padding(horizontal = 5.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .height(50.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Place, stringResource(R.string.place), tint = secondaryFigmaColor)
                Spacer(modifier = Modifier.width(150.dp))
                UserProfile(user)
            }
            Text(place.title, fontWeight = FontWeight.Bold)
            if (!place.description.isNullOrEmpty()) Text(place.description!!)
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}

@Composable
fun CatchInfo(catch: UserCatch, user: User?) {
    MyCardNoPadding {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp).padding(horizontal = 5.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth().height(50.dp)
            ) {
                Text(
                    catch.title,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Row( modifier = Modifier
                    .padding(horizontal = 10.dp).fillMaxHeight()) {
                UserProfile(user) }
            }
            if (!catch.description.isNullOrEmpty()) Text(
                catch.description, modifier = Modifier.fillMaxWidth(),
                fontSize = MaterialTheme.typography.button.fontSize
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(catch.time, fontSize = MaterialTheme.typography.caption.fontSize)
                Text(catch.date, fontSize = MaterialTheme.typography.caption.fontSize)
            }
        }
    }
}