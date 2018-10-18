package io.github.teitss.cashshop

import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

object LogRegistrar {

    private val logList = mutableListOf<String>()
    private val sdf = SimpleDateFormat("dd-MM-yyyy")
    private val sdf2 = SimpleDateFormat("HH:mm:ss")

    fun mapRecord(date: Date, logLine: String) {
        val info = "${sdf2.format(date)} - $logLine \n"
        logList.add(info)
    }

    fun registerRecords() {
        val file = File("${CashShop.instance.configDir}/logs/${sdf.format(Date())}.cslog")
        if (!file.exists()) {
            file.createNewFile()
        }
        BufferedWriter(OutputStreamWriter(FileOutputStream(file, true), "UTF8")).use {
            for (s in logList) {
                it.append(s)
            }
            logList.clear()
            CashShop.instance.logger.info("Os registros foram salvos.")
        }

    }

}
