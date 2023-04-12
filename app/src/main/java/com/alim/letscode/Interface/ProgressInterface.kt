package com.alim.letscode.Interface

interface ProgressInterface {
    fun Status(running: Boolean, pos: Int)
    fun Progress(total: Int, current: Int)
}