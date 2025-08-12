package ru.atrs.mcm.serial_port

import ru.atrs.mcm.ui.main_screen.center.support_elements.ch1
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch2
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch3
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch4
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch5
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch6
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch7
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch8
import ru.atrs.mcm.utils.indexOfScenario

//suspend fun reInitSolenoids() {
//    CommunicationMachineV1.comparatorToSolenoid(indexOfScenario.value)
//
//    CommunicationMachineV1.writeToSerialPort(
//        byteArrayOf(0x71, ch1, 0x00, ch2, 0x00, ch3, 0x00, ch4, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00),
//        delay = 100L
//    )
//
//    CommunicationMachineV1.writeToSerialPort(
//        byteArrayOf(0x51, ch5, 0x00, ch6, 0x00, ch7, 0x00, ch8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00),
//        delay = 0L
//    )
//
//}

//suspend fun manageSolenoids(isNextStep: Boolean) {
//    if (isNextStep) {
//
//        indexOfScenario++
//        if (indexOfScenario)
//    } else {
//        indexOfScenario--
//    }
//}