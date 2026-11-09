package com.jennyai.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JennyCommandInterpreterTest {
    @Test fun recognizesAddressedWalkCommand() {
        assertEquals("walk", JennyCommandInterpreter.parse("جنی راه برو")?.animationName)
    }

    @Test fun recognizesRunCommand() {
        assertEquals("run", JennyCommandInterpreter.parse("جنی بدو")?.animationName)
    }

    @Test fun recognizesStopCommand() {
        assertEquals("idle", JennyCommandInterpreter.parse("جنی بایست")?.animationName)
    }

    @Test fun normalizesArabicYehAndKaf() {
        assertEquals("walk", JennyCommandInterpreter.parse("جني راه برو")?.animationName)
        assertEquals("walk", JennyCommandInterpreter.parse("جنی برو")?.animationName)
    }

    @Test fun recognizesWaveAndJumpClipsPresentInRiggedModel() {
        assertEquals("wave", JennyCommandInterpreter.parse("جنی دست تکون بده")?.animationName)
        assertEquals("jump", JennyCommandInterpreter.parse("جنی بپر")?.animationName)
    }

    @Test fun doesNotMapUnsupportedSitOrTurnToMissingClips() {
        assertNull(JennyCommandInterpreter.parse("جنی بشین"))
        assertNull(JennyCommandInterpreter.parse("جنی برگرد"))
    }

    @Test fun doesNotTurnOrdinaryChatIntoMovement() {
        assertNull(JennyCommandInterpreter.parse("امروز هوا چطوره؟"))
    }

    @Test fun doesNotExecuteNegatedCommand() {
        assertNull(JennyCommandInterpreter.parse("جنی نمیخوام راه بری"))
    }
}
