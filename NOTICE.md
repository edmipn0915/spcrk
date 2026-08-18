# NOTICE

## Sparck 灵愿 — 第三方依赖许可证审计与清单

- 项目许可证：**Apache License 2.0**（见同目录 `LICENSE`）
- 审计日期：2026-08-18
- 审计方法：扫描 `app/build.gradle` 全部直接依赖，核对各组件官方许可证；传递依赖逐一确认
- 审计结论：**全项目 30 项依赖均为 Apache 2.0 / MIT / Unlicense，与 Apache 2.0 完全兼容**

---

## 一、构建插件

| 编号 | 依赖 | 许可证 |
|------|------|--------|
| 1 | `com.android.tools.build:gradle:8.1.0` | Apache 2.0 |
| 2 | `org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0` | Apache 2.0 |

## 二、UI / 运行时

| 编号 | 依赖 | 许可证 |
|------|------|--------|
| 3 | `androidx.core:core-ktx:1.12.0` | Apache 2.0 |
| 4 | `androidx.lifecycle:lifecycle-runtime-ktx:2.6.2` | Apache 2.0 |
| 5 | `androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2` | Apache 2.0 |
| 6 | `androidx.activity:activity-compose:1.8.0` | Apache 2.0 |
| 7 | `androidx.compose.ui:ui:1.5.0` | Apache 2.0 |
| 8 | `androidx.compose.ui:ui-tooling-preview:1.5.0` | Apache 2.0 |
| 9 | `androidx.compose.material:material-ripple:1.5.0` | Apache 2.0 |
| 10 | `androidx.compose.material3:material3:1.1.2` | Apache 2.0 |
| 11 | `androidx.compose.material:material-icons-extended:1.5.4` | Apache 2.0 |
| 12 | `androidx.navigation:navigation-compose:2.7.5` | Apache 2.0 |

## 三、网络 / 解析

| 编号 | 依赖 | 许可证 |
|------|------|--------|
| 13 | `com.squareup.okhttp3:okhttp:4.12.0`（含 okio） | Apache 2.0 |
| 14 | `com.google.code.gson:gson:2.10.1` | Apache 2.0 |
| 15 | `org.jsoup:jsoup:1.16.1` | MIT |

## 四、异步 / AI / 视频

| 编号 | 依赖 | 许可证 |
|------|------|--------|
| 16 | `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3` | Apache 2.0 |
| 17 | `com.google.mlkit:text-recognition:16.0.0` | Apache 2.0 |
| 18 | `com.google.mlkit:text-recognition-chinese:16.0.0` | Apache 2.0 |
| 19 | `com.google.android.gms:play-services-tasks:18.1.0` | Apache 2.0 |
| 20 | `com.github.sealedtx:java-youtube-downloader:3.2.3` | Unlicense |

## 五、数据持久化

| 编号 | 依赖 | 许可证 |
|------|------|--------|
| 21 | `androidx.room:room-runtime:2.6.1` | Apache 2.0 |
| 22 | `androidx.room:room-ktx:2.6.1` | Apache 2.0 |
| 23 | `androidx.room:room-compiler:2.6.1`（kapt） | Apache 2.0 |
| 24 | `androidx.work:work-runtime-ktx:2.9.0` | Apache 2.0 |

## 六、文档解析

| 编号 | 依赖 | 许可证 |
|------|------|--------|
| 25 | `org.apache.pdfbox:pdfbox:2.0.31`（含 fontbox） | Apache 2.0 |
| 26 | `org.apache.poi:poi:5.2.5` | Apache 2.0 |
| 27 | `org.apache.poi:poi-ooxml:5.2.5` | Apache 2.0 |
| 28 | `org.apache.poi:poi-scratchpad:5.2.5` | Apache 2.0 |

## 七、Debug 专用

| 编号 | 依赖 | 许可证 |
|------|------|--------|
| 29 | `androidx.compose.ui:ui-tooling:1.5.0` | Apache 2.0 |
| 30 | `androidx.compose.ui:ui-test-manifest:1.5.0` | Apache 2.0 |

---

## 传递依赖合规说明

以下传递依赖均已核实为宽松许可证（Apache 2.0 / MIT / BSD），无 copyleft 污染：
`okio`、`fontbox`、`commons-codec`、`commons-collections4`、`commons-compress`、`commons-io`、`commons-math3`、`commons-logging`、`xmlbeans`、`SparseBitSet`、`curvesapi`、`log4j-api`、`kotlin-stdlib`、`kotlinx-coroutines-core`。