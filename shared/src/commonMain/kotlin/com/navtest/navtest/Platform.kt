package com.navtest.navtest

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform