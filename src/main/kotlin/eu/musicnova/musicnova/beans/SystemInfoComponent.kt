package eu.musicnova.musicnova.beans

import com.jcabi.manifests.Manifests
import eu.musicnova.musicnova.utils.Const.BEAN_IS_BOOT_JAR
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import oshi.SystemInfo
import oshi.software.os.OperatingSystem
import javax.inject.Named

@Component
class SystemInfoComponent {

    private val logger = LoggerFactory.getLogger(SystemInfoComponent::class.java)

    @Bean
    fun oshiSystemInfo(): SystemInfo {
        logger.info("Init SystemInfo...")
        return SystemInfo()
    }

    @Bean
    fun oshiOperatingSystemInfo(systemInfo: SystemInfo): OperatingSystem = systemInfo.operatingSystem

    @Bean
    @Named(BEAN_IS_BOOT_JAR)
    fun isBootJar() =  runCatching { Manifests.read("BootJAR") }.getOrNull()?.equals("true", true) ?: false

}