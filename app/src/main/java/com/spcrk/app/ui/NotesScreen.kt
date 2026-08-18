package com.spcrk.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.spcrk.app.ui.navigation.Screen

@Composable
fun NotesScreen(
    navController: NavController,
    onBackClick: () -> Unit
) {
    NoteListScreen(
        onBackClick = onBackClick,
        onNoteClick = { noteId ->
            navController.navigate("${Screen.NoteEdit.route}/$noteId")
        },
        onNewNote = {
            navController.navigate("${Screen.NoteEdit.route}/new")
        }
    )
}
