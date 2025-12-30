package com.ipca.sassolidaria.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    data class ReportData(
        val title: String,
        val subtitle: String? = null,
        val generatedAt: Date = Date(),
        val blocks: List<Block>
    )

    sealed class Block {
        data class SectionTitle(val text: String) : Block()
        data class Paragraph(val lines: List<String>) : Block()
        data class Table(
            val title: String,
            val headers: List<String>,
            val rows: List<List<String>>
        ) : Block()
    }

    fun generateAndSave(context: Context, report: ReportData): Uri? {
        val doc = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842
        val margin = 32

        val green = Color.parseColor("#004B3C")

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = green
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 11.5f
        }
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            textSize = 10.5f
        }
        val h2Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 13.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 11.5f
        }

        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F4F7F6")
            style = Paint.Style.FILL
        }
        val cardStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D8E3DF")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val tableHeaderBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E3EFEA")
            style = Paint.Style.FILL
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D8E3DF")
            strokeWidth = 2f
        }

        val dateFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = margin

        fun newPage() {
            drawFooter(canvas, pageNumber, pageWidth, pageHeight, margin, smallPaint)
            doc.finishPage(page)
            pageNumber++
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            y = margin
        }

        fun ensureSpace(required: Int) {
            if (y + required > pageHeight - margin) newPage()
        }

        fun drawHeader() {
            canvas.drawText(report.title, margin.toFloat(), y.toFloat(), titlePaint)
            y += 22

            report.subtitle?.let {
                canvas.drawText(it, margin.toFloat(), y.toFloat(), subtitlePaint)
                y += 16
            }

            canvas.drawText("Gerado em: ${dateFmt.format(report.generatedAt)}", margin.toFloat(), y.toFloat(), smallPaint)
            y += 18

            canvas.drawLine(margin.toFloat(), y.toFloat(), (pageWidth - margin).toFloat(), y.toFloat(), linePaint)
            y += 16
        }

        fun drawSectionTitle(text: String) {
            ensureSpace(30)
            canvas.drawText(text, margin.toFloat(), y.toFloat(), h2Paint)
            y += 18
        }

        fun drawCard(blockHeight: Int, content: () -> Unit) {
            ensureSpace(blockHeight + 14)
            val left = margin.toFloat()
            val top = y.toFloat()
            val right = (pageWidth - margin).toFloat()
            val bottom = (y + blockHeight).toFloat()

            val r = RectF(left, top, right, bottom)
            canvas.drawRoundRect(r, 14f, 14f, cardPaint)
            canvas.drawRoundRect(r, 14f, 14f, cardStroke)

            val oldY = y
            y += 14
            content()
            y = maxOf(y, oldY + blockHeight)
            y += 14
        }

        fun drawParagraph(lines: List<String>) {
            val lineH = 16
            val needed = 14 + (lines.size * lineH) + 10
            drawCard(needed) {
                for (l in lines) {
                    canvas.drawText("• $l", (margin + 10).toFloat(), y.toFloat(), textPaint)
                    y += lineH
                }
            }
        }

        fun drawTable(title: String, headers: List<String>, rows: List<List<String>>) {
            val headerH = 22
            val rowH = 18
            val maxRows = 30

            val needed = 14 + 18 + headerH + (rows.size * rowH) + 20
            drawCard(needed) {

                canvas.drawText(title, (margin + 10).toFloat(), y.toFloat(), h2Paint)
                y += 16

                val tableLeft = (margin + 10).toFloat()
                val tableRight = (pageWidth - margin - 10).toFloat()
                val tableWidth = tableRight - tableLeft

                val cols = headers.size.coerceAtLeast(1)
                val colW = tableWidth / cols

                val headerRect = RectF(tableLeft, y.toFloat(), tableRight, (y + headerH).toFloat())
                canvas.drawRoundRect(headerRect, 10f, 10f, tableHeaderBg)

                val headerPaint = Paint(textPaint).apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                for (i in headers.indices) {
                    val x = tableLeft + i * colW + 6f
                    canvas.drawText(headers[i], x, (y + 15).toFloat(), headerPaint)
                }
                y += headerH

                rows.take(maxRows).forEach { row ->
                    for (i in 0 until cols) {
                        val value = row.getOrNull(i) ?: ""
                        val x = tableLeft + i * colW + 6f
                        canvas.drawText(ellipsize(value, 26), x, (y + 14).toFloat(), textPaint)
                    }
                    canvas.drawLine(tableLeft, (y + rowH).toFloat(), tableRight, (y + rowH).toFloat(), linePaint)
                    y += rowH
                }
            }
        }

        // Render
        drawHeader()
        report.blocks.forEach { b ->
            when (b) {
                is Block.SectionTitle -> drawSectionTitle(b.text)
                is Block.Paragraph -> drawParagraph(b.lines)
                is Block.Table -> drawTable(b.title, b.headers, b.rows)
            }
        }

        drawFooter(canvas, pageNumber, pageWidth, pageHeight, margin, smallPaint)
        doc.finishPage(page)

        // Save to Downloads
        val fileName = "Relatorio_LojaSocial_${System.currentTimeMillis()}.pdf"
        val resolver = context.contentResolver

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val uri = resolver.insert(collection, values) ?: run {
            doc.close()
            return null
        }

        return try {
            resolver.openOutputStream(uri)?.use { out ->
                doc.writeTo(out)
            }
            doc.close()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // marcar como concluído
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }

            uri
        } catch (_: Exception) {
            doc.close()
            // tenta remover o ficheiro incompleto
            runCatching { resolver.delete(uri, null, null) }
            null
        }
    }

    private fun drawFooter(
        canvas: Canvas,
        pageNumber: Int,
        pageWidth: Int,
        pageHeight: Int,
        margin: Int,
        paint: Paint
    ) {
        val footer = "Página $pageNumber"
        val w = paint.measureText(footer)
        canvas.drawText(footer, (pageWidth - margin - w), (pageHeight - margin / 2).toFloat(), paint)
    }

    private fun ellipsize(s: String, max: Int): String {
        val t = s.trim()
        if (t.length <= max) return t
        return t.take(max - 1) + "…"
    }
}
