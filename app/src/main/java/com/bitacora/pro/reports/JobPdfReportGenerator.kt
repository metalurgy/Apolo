package com.bitacora.pro.reports

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.bitacora.pro.data.models.AgendaStatus
import com.bitacora.pro.data.models.EvidenceType
import com.bitacora.pro.data.models.JobFile
import com.bitacora.pro.data.storage.StorageManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Generates professional PDF reports for jobs.
 * Uses Android's built-in PdfDocument API with proper pagination and resource management.
 *
 * Features:
 * - Multi-page support with automatic page breaks
 * - Safe bitmap resource handling with proper cleanup
 * - Improved evidence rendering with better spacing
 * - Friendly file names with job title
 */
object JobPdfReportGenerator {

    private const val PAGE_WIDTH = 595  // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 40
    private const val CONTENT_WIDTH = PAGE_WIDTH - (2 * MARGIN)
    private const val MIN_SPACE_FOR_CONTENT = 100  // Minimum space needed before page break

    /**
     * Generates a PDF report for the given job.
     * Returns the generated File if successful, null otherwise.
     * Handles multi-page documents with proper pagination.
     */
    fun generateReport(
        context: Context,
        job: JobFile,
        storageManager: StorageManager
    ): File? {
        var pdfDocument: PdfDocument? = null
        return try {
            pdfDocument = PdfDocument()
            var pageNumber = 1
            var yPosition = MARGIN
            var currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            var currentCanvas = currentPage.canvas

            // Draw header
            yPosition = drawHeader(currentCanvas, yPosition)
            yPosition += 20

            // Draw job metadata
            yPosition = drawJobMetadata(currentCanvas, job, yPosition)
            yPosition += 20

            // Draw agenda section
            yPosition = drawAgendaSection(currentCanvas, job, yPosition)
            yPosition += 20

            // Check if we need a new page for evidence
            if (yPosition > PAGE_HEIGHT - MIN_SPACE_FOR_CONTENT) {
                drawFooter(currentCanvas, PAGE_HEIGHT - 30)
                pdfDocument.finishPage(currentPage)
                pageNumber++
                currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                currentCanvas = currentPage.canvas
                yPosition = MARGIN
            }

            // Draw evidence section with page break support
            val evidenceResult = drawEvidenceSectionWithPagination(
                pdfDocument, currentCanvas, currentPage, pageNumber,
                job, storageManager, yPosition
            )
            
            if (evidenceResult != null) {
                pageNumber = evidenceResult.pageNumber
                currentPage = evidenceResult.currentPage
                currentCanvas = evidenceResult.currentCanvas
                yPosition = evidenceResult.yPosition
            }

            // Draw footer on last page
            drawFooter(currentCanvas, PAGE_HEIGHT - 30)
            pdfDocument.finishPage(currentPage)

            // Save the PDF with friendly name using safe stream handling
            val reportsDir = storageManager.getReportsDir(job.id)
            val fileName = generateFriendlyFileName(job)
            val reportFile = File(reportsDir, fileName)

            reportFile.outputStream().use { output ->
                pdfDocument.writeTo(output)
            }
            reportFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            pdfDocument?.close()
        }
    }

    /**
     * Generates a friendly PDF file name based on job title and timestamp.
     */
    private fun generateFriendlyFileName(job: JobFile): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val cleanTitle = job.title
            .replace(Regex("[^a-zA-Z0-9_-]"), "_")
            .take(30)
        return "Reporte_${cleanTitle}_$timestamp.pdf"
    }

    /**
     * Data class to track pagination state across page breaks.
     */
    private data class PaginationState(
        val pageNumber: Int,
        val currentPage: PdfDocument.Page,
        val currentCanvas: Canvas,
        val yPosition: Int
    )

    /**
     * Draws evidence section with automatic page breaks.
     * Returns pagination state for continuing on next page, or null if no evidence.
     */
    private fun drawEvidenceSectionWithPagination(
        pdfDocument: PdfDocument,
        initialCanvas: Canvas,
        initialPage: PdfDocument.Page,
        initialPageNumber: Int,
        job: JobFile,
        storageManager: StorageManager,
        initialY: Int
    ): PaginationState? {
        var pageNumber = initialPageNumber
        var currentPage = initialPage
        var currentCanvas = initialCanvas
        var yPosition = initialY

        val headerPaint = Paint().apply {
            color = Color.parseColor("#00897B")
            textSize = 14f
            isFakeBoldText = true
        }

        val itemPaint = Paint().apply {
            color = Color.parseColor("#424242")
            textSize = 11f
        }

        val metaPaint = Paint().apply {
            color = Color.parseColor("#757575")
            textSize = 10f
        }

        // Section header
        currentCanvas.drawText("Evidencia (${job.evidence.size})", MARGIN.toFloat(), yPosition.toFloat(), headerPaint)
        yPosition += 18

        if (job.evidence.isEmpty()) {
            itemPaint.color = Color.parseColor("#9E9E9E")
            currentCanvas.drawText("Sin evidencia", MARGIN.toFloat(), yPosition.toFloat(), itemPaint)
            yPosition += 16
        } else {
            job.evidence.forEach { evidence ->
                // Estimate block height for this evidence item
                var estimatedHeight = 14 // Type/category line
                if (evidence.fileName.isNotEmpty()) estimatedHeight += 12
                estimatedHeight += 12 // Date line
                if (evidence.type == EvidenceType.TEXT && evidence.textContent.isNotEmpty()) estimatedHeight += 12
                if (evidence.type == EvidenceType.IMAGE) estimatedHeight += 200 // Image thumbnail
                estimatedHeight += 8 // Spacing

                // Check if we need a new page before drawing this evidence item
                // Move whole item to next page if it doesn't fit
                if (yPosition + estimatedHeight > PAGE_HEIGHT - MIN_SPACE_FOR_CONTENT) {
                    drawFooter(currentCanvas, PAGE_HEIGHT - 30)
                    pdfDocument.finishPage(currentPage)
                    pageNumber++
                    currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                    currentCanvas = currentPage.canvas
                    yPosition = MARGIN
                }

                // Evidence type and category
                itemPaint.color = Color.parseColor("#424242")
                val typeLabel = evidence.type.name
                val categoryLabel = evidence.category.name
                currentCanvas.drawText("• $typeLabel - $categoryLabel", (MARGIN + 10).toFloat(), yPosition.toFloat(), itemPaint)
                yPosition += 14

                // File name or type
                if (evidence.fileName.isNotEmpty()) {
                    metaPaint.color = Color.parseColor("#757575")
                    currentCanvas.drawText("  ${evidence.fileName}", (MARGIN + 10).toFloat(), yPosition.toFloat(), metaPaint)
                    yPosition += 12
                }

                // Date
                metaPaint.color = Color.parseColor("#9E9E9E")
                currentCanvas.drawText("  ${formatDate(evidence.createdAt)}", (MARGIN + 10).toFloat(), yPosition.toFloat(), metaPaint)
                yPosition += 12

                // Text preview for TEXT evidence
                if (evidence.type == EvidenceType.TEXT && evidence.textContent.isNotEmpty()) {
                    metaPaint.color = Color.parseColor("#616161")
                    val preview = evidence.textContent.take(80) + if (evidence.textContent.length > 80) "..." else ""
                    currentCanvas.drawText("  \"$preview\"", (MARGIN + 10).toFloat(), yPosition.toFloat(), metaPaint)
                    yPosition += 12
                }

                // Try to draw image thumbnail for IMAGE evidence with safe resource handling
                if (evidence.type == EvidenceType.IMAGE) {
                    val file = storageManager.getEvidenceFile(job.id, evidence.id)
                    if (file != null && file.exists()) {
                        var bitmap: android.graphics.Bitmap? = null
                        try {
                            bitmap = BitmapFactory.decodeFile(file.absolutePath)
                            if (bitmap != null) {
                                // Downsample if needed
                                val scaledBitmap = if (bitmap.width > 200 || bitmap.height > 200) {
                                    val scale = minOf(200f / bitmap.width, 200f / bitmap.height)
                                    val scaledWidth = (bitmap.width * scale).toInt()
                                    val scaledHeight = (bitmap.height * scale).toInt()
                                    android.graphics.Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
                                } else {
                                    bitmap
                                }

                                // Draw thumbnail
                                currentCanvas.drawBitmap(scaledBitmap, (MARGIN + 10).toFloat(), yPosition.toFloat(), null)
                                yPosition += scaledBitmap.height + 8

                                // Clean up scaled bitmap if it's different from original
                                if (scaledBitmap != bitmap) {
                                    scaledBitmap.recycle()
                                }
                            } else {
                                // Fallback for corrupt/unreadable images
                                metaPaint.color = Color.parseColor("#9E9E9E")
                                currentCanvas.drawText("  [Imagen no disponible]", (MARGIN + 10).toFloat(), yPosition.toFloat(), metaPaint)
                                yPosition += 12
                            }
                        } catch (e: Exception) {
                            // Fallback for image loading errors
                            metaPaint.color = Color.parseColor("#9E9E9E")
                            currentCanvas.drawText("  [Imagen no disponible]", (MARGIN + 10).toFloat(), yPosition.toFloat(), metaPaint)
                            yPosition += 12
                            e.printStackTrace()
                        } finally {
                            // Always recycle the original bitmap
                            bitmap?.recycle()
                        }
                    } else {
                        // Fallback for missing image files
                        metaPaint.color = Color.parseColor("#9E9E9E")
                        currentCanvas.drawText("  [Imagen no disponible]", (MARGIN + 10).toFloat(), yPosition.toFloat(), metaPaint)
                        yPosition += 12
                    }
                }

                yPosition += 8
            }
        }

        return PaginationState(pageNumber, currentPage, currentCanvas, yPosition)
    }

    private fun drawHeader(canvas: Canvas, startY: Int): Int {
        val titlePaint = Paint().apply {
            color = Color.parseColor("#00897B")
            textSize = 28f
            isFakeBoldText = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#00897B")
            textSize = 18f
        }

        canvas.drawText("Bitacora Pro", MARGIN.toFloat(), startY.toFloat(), titlePaint)
        canvas.drawText("Reporte de Trabajo", MARGIN.toFloat(), (startY + 30).toFloat(), subtitlePaint)

        return startY + 50
    }

    private fun drawJobMetadata(canvas: Canvas, job: JobFile, startY: Int): Int {
        val headerPaint = Paint().apply {
            color = Color.parseColor("#00897B")
            textSize = 14f
            isFakeBoldText = true
        }

        val labelPaint = Paint().apply {
            color = Color.parseColor("#424242")
            textSize = 11f
            isFakeBoldText = true
        }

        val valuePaint = Paint().apply {
            color = Color.parseColor("#616161")
            textSize = 11f
        }

        var yPos = startY

        // Section header
        canvas.drawText("Información del Trabajo", MARGIN.toFloat(), yPos.toFloat(), headerPaint)
        yPos += 18

        // Draw metadata rows
        yPos = drawMetadataRow(canvas, "Título:", job.title, yPos, labelPaint, valuePaint)
        yPos = drawMetadataRow(canvas, "Cliente:", job.clientName, yPos, labelPaint, valuePaint)
        yPos = drawMetadataRow(canvas, "Teléfono:", job.phone, yPos, labelPaint, valuePaint)
        yPos = drawMetadataRow(canvas, "Servicio:", job.serviceType, yPos, labelPaint, valuePaint)
        yPos = drawMetadataRow(canvas, "Estado:", job.status.name, yPos, labelPaint, valuePaint)
        yPos = drawMetadataRow(canvas, "Creado:", formatDate(job.createdAt), yPos, labelPaint, valuePaint)
        yPos = drawMetadataRow(canvas, "Actualizado:", formatDate(job.updatedAt), yPos, labelPaint, valuePaint)

        return yPos
    }

    private fun drawMetadataRow(
        canvas: Canvas,
        label: String,
        value: String,
        yPos: Int,
        labelPaint: Paint,
        valuePaint: Paint
    ): Int {
        canvas.drawText(label, MARGIN.toFloat(), yPos.toFloat(), labelPaint)
        canvas.drawText(value, (MARGIN + 120).toFloat(), yPos.toFloat(), valuePaint)
        return yPos + 16
    }

    private fun drawAgendaSection(canvas: Canvas, job: JobFile, startY: Int): Int {
        val headerPaint = Paint().apply {
            color = Color.parseColor("#00897B")
            textSize = 14f
            isFakeBoldText = true
        }

        val itemPaint = Paint().apply {
            color = Color.parseColor("#424242")
            textSize = 11f
        }

        val statusPaint = Paint().apply {
            color = Color.parseColor("#757575")
            textSize = 10f
        }

        var yPos = startY

        // Filter out archived items - only show pending and done
        val activeItems = job.agendaItems.filter { it.status != AgendaStatus.ARCHIVED }
        
        // Section header
        canvas.drawText("Agenda (${activeItems.size})", MARGIN.toFloat(), yPos.toFloat(), headerPaint)
        yPos += 18

        if (activeItems.isEmpty()) {
            itemPaint.color = Color.parseColor("#9E9E9E")
            canvas.drawText("Sin elementos de agenda", MARGIN.toFloat(), yPos.toFloat(), itemPaint)
            yPos += 16
        } else {
            // Pending items
            val pendingItems = activeItems.filter { it.status == AgendaStatus.PENDING }
            if (pendingItems.isNotEmpty()) {
                statusPaint.color = Color.parseColor("#F57C00")
                canvas.drawText("Pendiente", MARGIN.toFloat(), yPos.toFloat(), statusPaint)
                yPos += 14

                pendingItems.forEach { item ->
                    yPos = drawAgendaItem(canvas, item, yPos, itemPaint, statusPaint)
                }
                yPos += 8
            }

            // Done items
            val doneItems = activeItems.filter { it.status == AgendaStatus.DONE }
            if (doneItems.isNotEmpty()) {
                statusPaint.color = Color.parseColor("#388E3C")
                canvas.drawText("Completado", MARGIN.toFloat(), yPos.toFloat(), statusPaint)
                yPos += 14

                doneItems.forEach { item ->
                    yPos = drawAgendaItem(canvas, item, yPos, itemPaint, statusPaint)
                }
            }
        }

        return yPos
    }

    private fun drawAgendaItem(
        canvas: Canvas,
        item: com.bitacora.pro.data.models.AgendaItem,
        yPos: Int,
        itemPaint: Paint,
        statusPaint: Paint
    ): Int {
        var y = yPos
        itemPaint.color = Color.parseColor("#424242")
        canvas.drawText("• ${item.title}", (MARGIN + 10).toFloat(), y.toFloat(), itemPaint)
        y += 14

        if (item.dueText.isNotEmpty()) {
            statusPaint.color = Color.parseColor("#757575")
            canvas.drawText("  Vencimiento: ${item.dueText}", (MARGIN + 10).toFloat(), y.toFloat(), statusPaint)
            y += 12
        }

        if (item.description.isNotEmpty()) {
            statusPaint.color = Color.parseColor("#9E9E9E")
            val desc = item.description.take(60) + if (item.description.length > 60) "..." else ""
            canvas.drawText("  $desc", (MARGIN + 10).toFloat(), y.toFloat(), statusPaint)
            y += 12
        }

        return y
    }

    private fun drawFooter(canvas: Canvas, yPos: Int) {
        val footerPaint = Paint().apply {
            color = Color.parseColor("#BDBDBD")
            textSize = 9f
        }

        val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generated by Bitacora Pro v0.6 - $timestamp", MARGIN.toFloat(), yPos.toFloat(), footerPaint)
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
