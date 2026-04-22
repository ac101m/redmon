package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v2.PersistentProfileV2

class Profile(internal var name: String, initPages: List<ProfilePage>) {
    val pages = initPages.toMutableList()
    private var currentPageIndex: Int

    init {
        if (pages.isEmpty()) {
            pages.add(ProfilePage("new_page", emptyList()))
        }
        currentPageIndex = 0
    }

    fun getCurrentPage(): ProfilePage {
        return pages[currentPageIndex]
    }

    fun getPageCount(): Int {
        return pages.size
    }

    fun getCurrentPageNumber(): Int {
        return currentPageIndex + 1
    }

    fun nextPage() {
        currentPageIndex = (currentPageIndex + 1) % pages.size
    }

    fun previousPage() {
        currentPageIndex = when (currentPageIndex) {
            0 -> pages.size - 1
            else -> currentPageIndex - 1
        }
    }

    fun addPage(name: String) {
        pages.add(ProfilePage(name, emptyList()))
        currentPageIndex = pages.size - 1
    }

    fun removePage(pageName: String) {
        requireNotNull(pages.find { it.name == pageName }) {
            "Profile '$name' does not contain a page with name '$pageName'"
        }
        pages.removeIf { it.name == pageName }
        currentPageIndex %= pages.size
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
