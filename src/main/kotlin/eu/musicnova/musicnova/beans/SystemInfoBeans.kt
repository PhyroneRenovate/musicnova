package eu.musicnova.musicnova.beans

import com.jcabi.manifests.Manifests
import eu.musicnova.musicnova.utils.Const.BEAN_IS_BOOT_JAR
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import oshi.SystemInfo
import oshi.software.os.OSProcess
import oshi.software.os.OperatingSystem

@Component
class SystemInfoBeans {

    private val logger = LoggerFactory.getLogger(SystemInfoBeans::class.java)

    @Bean
    fun oshiSystemInfo(): SystemInfo {
        logger.info("Init SystemInfo...")
        return SystemInfo()
    }

    @Bean
    fun operatingSystemInfo(systemInfo: SystemInfo): OperatingSystem = systemInfo.operatingSystem

    @Bean
    fun processInfo(operatingSystem: OperatingSystem):OSProcess = operatingSystem.getProcess(operatingSystem.processId)

    @Bean
    @Qualifier(BEAN_IS_BOOT_JAR)
    fun isBootJar() =  runCatching { Manifests.read("BootJAR") }.getOrNull()?.equals("true", true) ?: false

}