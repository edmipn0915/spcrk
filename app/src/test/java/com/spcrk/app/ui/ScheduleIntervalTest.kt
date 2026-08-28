package com.spcrk.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 定時任務間隔換算的純邏輯測試。
 * WorkManager 週期性工作最小間隔為 15 分鐘，低於此值的 cron 必須被夾緊，
 * 否則 enqueue 時拋 IllegalArgumentException 造成 App 崩潰。
 */
class ScheduleIntervalTest {

    @Test
    fun `every minute cron clamps to 15 minutes`() {
        assertEquals(15L, intervalMinutesForCron("* * * * *"))
    }

    @Test
    fun `every 5 minutes cron clamps to 15 minutes`() {
        assertEquals(15L, intervalMinutesForCron("*/5 * * * *"))
    }

    @Test
    fun `every 15 minutes cron stays 15`() {
        assertEquals(15L, intervalMinutesForCron("*/15 * * * *"))
    }

    @Test
    fun `hourly cron maps to 60`() {
        assertEquals(60L, intervalMinutesForCron("0 * * * *"))
    }

    @Test
    fun `daily cron maps to 1440`() {
        assertEquals(1440L, intervalMinutesForCron("0 9 * * *"))
    }

    @Test
    fun `custom cron maps to default 60`() {
        assertEquals(60L, intervalMinutesForCron("0 0 1 * *"))
    }
}
