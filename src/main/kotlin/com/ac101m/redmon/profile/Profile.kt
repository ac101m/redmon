package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v2.PersistentProfileV2

class Profile(internal var name: String, initPages: List<ProfilePage>) {
    private val pages = initPages.toMutableList()
    private var currentPageIndex: Int

    init {
        if (pages.isEmpty()) {
            pages.add(ProfilePage("Page 1", emptyList()))
        }
        currentPageIndex = 0
    }

    fun getCurrentPage(): ProfilePage {
        return pages[currentPageIndex]
    }

    fun getPageCount(): Int {
        return pages.size
    }

    fun getCurrentPageIndex(): Int {
        return currentPageIndex + 1
    }

    // Function to go to next page
    fun nextPage() {
        currentPageIndex = (currentPageIndex + 1) % pages.size
    }

    // Function to go to previous page
    fun previousPage() {
        currentPageIndex = when (currentPageIndex) {
            0 -> pages.size - 1
            else -> currentPageIndex - 1
        }
    }

    // Function to add new page
    fun addPage(name: String) {
        pages.add(ProfilePage(name, emptyList()))
    }

    fun toPersistentProfile(): PersistentProfileV2 {
        val persistentPages = pages.map { page -> page.toPersistentProfilePage() }
        return PersistentProfileV2(name, persistentPages)
    }

    companion object {
        fun fromPersistentProfile(persistentProfile: PersistentProfileV2): Profile {
            val pages = persistentProfile.pages.map { persistentProfilePage ->
                ProfilePage.fromPersistentProfilePage(persistentProfilePage)
            }
            return Profile(persistentProfile.name, pages)
        }
    }
}
