package com.daycountapp.data

import com.daycountapp.data.model.Event

object DayFileSerializer {
    private const val HEADER = "DAYCOUNT_EXPORT_V1"

    fun serialize(events: List<Event>): String {
        val sb = StringBuilder()
        sb.appendLine(HEADER)
        sb.appendLine("exported_at=${System.currentTimeMillis()}")
        sb.appendLine("event_count=${events.size}")

        events.forEach { e ->
            sb.appendLine("event:id=${e.id}")
            sb.appendLine("title=${e.title.escape()}")
            sb.appendLine("desc=${e.description.escape()}")
            sb.appendLine("date=${e.targetDate}")
            sb.appendLine("flags=${(if (e.isCountUp) 1 else 0) or (if (e.isHidden) 2 else 0)}")
            sb.appendLine("color=${e.colorPreset}")
            sb.appendLine("custom_color=${e.customColorArgb}")
            sb.appendLine("grad_start=${e.colorPresetEnd}")
            sb.appendLine("grad_end=${e.gradientDirection}")
            sb.appendLine("created=${e.createTime}")
            sb.appendLine("updated=${e.updateTime}")
            sb.appendLine("---")
        }

        return sb.toString()
    }

    fun deserialize(content: String): Result<List<Event>> =
        runCatching {
            val lines = content.lines().iterator()

            if (!lines.hasNext()) error("空文件")
            val header = lines.next()
            if (header != HEADER) error("不支持的文件格式")

            var eventCount = 0
            val events = mutableListOf<Event>()

            while (lines.hasNext()) {
                val line = lines.next().trim()
                when {
                    line.startsWith("event_count=") -> {
                        eventCount = line.substringAfter('=').toInt()
                    }

                    line.startsWith("event:") -> {
                        events.add(parseEvent(lines))
                    }
                }
            }

            if (events.size != eventCount) error("事件数量不匹配")
            events
        }

    private fun parseEvent(lines: Iterator<String>): Event {
        var id = 0L
        var title = ""
        var desc = ""
        var date = 0L
        var flags = 0
        var color = -1
        var customColor = 0
        var gradStart = -1
        var gradEnd = 0
        var created = 0L
        var updated = 0L

        while (lines.hasNext()) {
            val line = lines.next().trim()
            if (line.isEmpty()) break
            when {
                line.startsWith("id=") -> id = line.substringAfter('=').toLong()
                line.startsWith("title=") -> title = line.substringAfter('=').unescape()
                line.startsWith("desc=") -> desc = line.substringAfter('=').unescape()
                line.startsWith("date=") -> date = line.substringAfter('=').toLong()
                line.startsWith("flags=") -> flags = line.substringAfter('=').toInt()
                line.startsWith("color=") -> color = line.substringAfter('=').toInt()
                line.startsWith("custom_color=") -> customColor = line.substringAfter('=').toInt()
                line.startsWith("grad_start=") -> gradStart = line.substringAfter('=').toInt()
                line.startsWith("grad_end=") -> gradEnd = line.substringAfter('=').toInt()
                line.startsWith("created=") -> created = line.substringAfter('=').toLong()
                line.startsWith("updated=") -> updated = line.substringAfter('=').toLong()
            }
        }

        return Event(
            id = id,
            title = title,
            description = desc,
            targetDate = date,
            isCountUp = flags and 1 != 0,
            isHidden = flags and 2 != 0,
            colorPreset = color,
            customColorArgb = customColor,
            gradientDirection = gradEnd,
            createTime = created,
            updateTime = updated,
        )
    }

    private fun String.escape() = replace("\n", "\\n").replace("=", "\\=")

    private fun String.unescape() = replace("\\=", "=").replace("\\n", "\n")
}
