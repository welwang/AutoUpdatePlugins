# 評價、討論與 GitHub Issues 檢查紀錄

檢查日期：2026-10-26（台北）。上游基準：`984818f7b9bbf7b750ea82d81a89803814729707`。

涵蓋 [Spigot 評價](https://www.spigotmc.org/resources/autoupdateplugins.109683/reviews) 3 則、[討論串](https://www.spigotmc.org/threads/autoupdateplugins.603327/) 28 篇貼文，以及 [GitHub Issues](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues) 全部 70 個非 PR 問題與 285 則後續留言。原始內容下載於本機 research，沒有把留言全文打包或上傳。

這份清單區分本次修正、上游已提供的功能與尚未加入的需求；「上游已有」不代表所有外部下載站都經過實機測試。已關閉的問題也檢查後續回報，不單憑關閉狀態判定已解決。

## 本次新增及修正

- 固定每日多時刻、台北時區預設、cron／間隔模式、指令設定與立即替換舊排程，關閉排程仍可手動更新。
- 依已安裝檔案雜湊或內嵌官方網址尋找來源；無法確認時僅列候選，不憑同名自動安裝。
- 缺少的插件預設不安裝；新安裝需明確 `installMissing=true`，空清單也不會重新補入預設插件。
- 下載候選與已安裝插件身分不同時，阻擋覆寫；`force` 也不能繞過。
- `updatePath` 單獨指定時啟用暫存更新，保留 `useUpdateFolder=false` 的明確覆寫。
- Geyser 擴充目錄推斷 `geyser` 平台；明確 loader 仍優先。加入 Geyser 擴充描述檔及 Paper 描述檔的識別。
- Jenkins JSON API 404／405 時解析同一官方工作頁面的 JAR 連結，取得固定 build 編號；不將外站連結當成下載候選。
- 打包移除上游開發機的固定複製路徑，GitHub 建置改為手動觸發。

## Spigot 評價與討論

| 回報 | 處理 |
|---|---|
| AutoViaUpdater 刪除後反覆被下載，且版本不可用 | 本次改為缺少插件跳過，增加身分比對，避免下載另一插件覆寫。刪除插件時已有 update 暫存檔也需一併移除。 |
| Discord 邀請失效或帳號被封鎖 | 實際查詢舊邀請回傳 404。說明改以 GitHub Issues 為支援入口；無法替原作者建立永久邀請或解除封鎖。 |
| 另一則為正面評價，未提出缺陷 | 無需修改。 |
| 討論串要求付費插件自動更新 | 保留有授權的平台支援；無授權時不繞過付費下載。 |
| 轉載 MineBBS 的請求 | 屬授權／社群問題，保留 MIT 及原作者資訊。 |
| 其餘討論主要為版本發布公告 | 核對公告提及的功能，使用最新上游原始碼作為基準。 |

## GitHub 全部問題對照

下列編號連結至上游原始問題。尚未加入的額外功能與外部條件會明確標示。

| 問題 | 檢查／處理結果 |
|---|---|
| [#1](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/1) Velocity 支援 | 上游已有，保留。 |
| [#2](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/2) GitHub／Jenkins 最新版本 API | 上游已有；本次補 Jenkins HTML 備援。 |
| [#3](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/3) 更新版本 | 使用上游 12.3.0 原始碼基準，產出獨立客製版。 |
| [#4](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/4) 來源、多檔、相容性與手動控制 | 上游已有多來源與篩選；本次再加入來源辨識與排程。授權問題保留 MIT。 |
| [#5](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/5) 手動更新指令 | 保留 update／download／check。 |
| [#6](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/6) 開機即更新與固定時間 | 本次預設固定時刻，不再依開機延遲執行；亦可關閉排程。 |
| [#8](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/8) Spiget 短網址與記錄 | 上游已有資源 ID 擷取與主流程 logger；外站限制仍需正常回應。 |
| [#9](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/9) 多下載檔與平台 | 保留 get 正規表示式、序號及 Modrinth 平台篩選；本次新增身分保護。 |
| [#10](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/10) Modrinth／CurseForge | 上游已有；保留外部 API／下載授權限制。 |
| [#12](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/12) GitHub Actions | 上游已有；部分下載需 token，並未移除授權要求。 |
| [#13](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/13) Geyser 檔名／ZIP | 保留上游 JAR／ZIP 處理與重複內容比對。 |
| [#14](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/14) Citizens Jenkins | 本次確認網頁 200、JSON 404，加入官方 HTML 備援與工作網址正規化。 |
| [#15](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/15) blob／BusyBiscuit | 上游已有，保留；未逐站實測所有專案。 |
| [#18](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/18) 預發行版 | 保留 prerelease／alpha／beta／latest 選項。 |
| [#19](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/19) 自我更新／使用中的檔案 | 使用 update 暫存機制；客製版不自動加入上游來源。既有自我更新項目需自行停用。 |
| [#20](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/20) Velocity 載入失敗 | 保留上游載入修正。未實際重啟 Velocity。 |
| [#21](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/21) Velocity authors 格式 | 使用上游註解處理器產生描述檔，保留修正。 |
| [#22](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/22) 標準輸出警告 | 主更新流程使用 logger；設定讀取失敗時上游仍有 System.err 訊息，未宣稱完全消除。 |
| [#23](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/23) AutoViaUpdater 寫入權限 | 屬另一插件與主機檔案權限，未更改系統權限。缺少插件跳過可避免意外重裝。 |
| [#24](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/24) 管理指令 | 保留清單、啟停、手動更新等功能，增加 schedule／discover。 |
| [#25](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/25) ZIP 解壓 | 保留上游封裝辨識與 JAR 提取。 |
| [#26](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/26) Actions 多 JAR | 保留選檔與上游測試；選錯其他插件時新增身分阻擋。 |
| [#27](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/27) 更新工作重疊 | 保留上游單次更新鎖，排程重載替換舊工作。 |
| [#28](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/28) 過期 Discord 下載／暫存路徑 | 改用 GitHub Release 交付，預設 update 暫存。 |
| [#29](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/29) 損毀下載覆寫 | 保留上游大小／雜湊／JAR 驗證及原子移動，再加入身分驗證。 |
| [#30](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/30) 啟動失敗還原 | 保留實驗性 rollback；整個伺服器無法啟動時插件本身不能執行還原。 |
| [#31](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/31) Modrinth get 選錯檔 | 保留上游正規表示式與相容版本篩選，本次核心檢查通過。 |
| [#33](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/33) Grim 平台／預發行選錯 | 保留平台、預發行與檔案篩選；不保證不同 MC 版本的 Grim 執行相容性。 |
| [#34](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/34) 本地檔案／腳本 | 上游已有，保留。 |
| [#35](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/35) Velocity 更新後重啟 | 上游已有可選設定，預設關閉。 |
| [#36](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/36) 換行格式 | 保留上游設定換行處理；來源新增也保留 CRLF／LF。 |
| [#37](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/37) Modrinth 舊版本 | 保留依日期排序及相容性篩選；本次補 Geyser 路徑推斷。 |
| [#38](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/38) cron 重複訊息 | 新排程依下一次時刻單次註冊，重新載入失效舊回呼。 |
| [#39](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/39) 同檔反覆暫存／重啟迴圈 | 保留上游內容比對與版本快取，驗證重複來源不下載、不標記更新。 |
| [#40](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/40) 單項路徑 | 保留 per-entry 設定；本次明確 updatePath 會啟用暫存。 |
| [#41](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/41) ignoreDuplicates | 保留比對 live／staged 檔案，檢查相同版本跳過。 |
| [#42](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/42) 重啟前指令 | 上游已有 preRestartCommand／restartCommands，保留。 |
| [#43](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/43) BuiltByBit | 尚未新增專用 provider；可使用合法官方直連／自訂來源，仍需平台授權。 |
| [#44](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/44) 自動編譯分支 | 保留 branch 選項與上游自動編譯功能。 |
| [#45](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/45) 更新伺服器核心 JAR | 尚未加入；本次處理 plugins 更新，不會替換 Paper／Velocity 核心。 |
| [#46](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/46) 群組 | 上游已有，保留。 |
| [#47](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/47) 僅檢查／人工下載 | 保留 manualMode、check、pending、update 與 auto 單項設定。 |
| [#48](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/48) MC 相容性 | 保留 Modrinth／Hangar 的平台與 MC 版本篩選。 |
| [#49](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/49) 大小版本政策 | 保留 any／patch／same-major／none；並非所有站都提供可比較版本。 |
| [#50](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/50) 付費來源通知／下載 | 上游 VoxelShop 可授權下載或人工通知；並未新增所有付費平台的通用自動更新。 |
| [#51](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/51) Polymart／VoxelShop | 保留上游 VoxelShop provider 與權杖支援。 |
| [#52](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/52) update 未下載 | 保留手動 install 與 scheduled manualMode 的區分，本次核心安裝流程通過。 |
| [#53](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/53) 寫入位置／舊版檔名 | 保留上游插件識別及 staging，補明確 updatePath 行為。 |
| [#54](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/54) 引號與路徑解析 | 保留上游 EntryOptions／EntryPathOptions；不把 updatePath= 當成目錄前綴。 |
| [#55](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/55) GUI | 保留 Spigot／Paper／Folia 管理 GUI。 |
| [#56](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/56) 版本更新紀錄 | 保留上游來源 release notes 與版本範圍摘要。 |
| [#57](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/57) 日誌／分頁 | 保留每日記錄與 log 指令。 |
| [#58](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/58) 編譯失敗／Jenkins／嵌套 JAR | Jenkins 補 HTML 備援與身分保護；編譯仍需合適 JDK、Maven／Gradle 及私有依賴，無法保證任意專案可編譯。 |
| [#59](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/59) GitLab | 保留上游 packages provider 與 token 支援。 |
| [#60](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/60) 多帳號 token | 上游已有命名 token／account／author 選項，保留。 |
| [#61](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/61) 擴充路徑 | 保留自訂 filePath／updatePath，補 Geyser 描述檔識別。 |
| [#62](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/62) 推薦與核心更新建議 | 致謝／額外功能建議，未新增核心更新器。 |
| [#63](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/63) 編譯暫存／檢查頻率 | 保留上游暫存清理；本次加入明確固定時間與 status。 |
| [#64](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/64) 代理指令攔截後端 | 保留上游代理獨立 vaup／baup，不占用後端 aup。 |
| [#65](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/65) 已停用仍 pending | 保留上游 retainPendingUpdates；來源辨識也不重新啟用已停用項目。 |
| [#66](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/66) PlaceholderAPI eCloud | 上游已有 ExtendedClipProvider，保留。 |
| [#67](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/67) 下載未變檔浪費流量 | 保留 metadata 快取，核心檢查確認第二次相同版本不抓 payload。 |
| [#68](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/68) Modrinth 平台覆寫 | 保留 loader／platform；本次增加 Geyser 路徑推斷。 |
| [#70](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/70) 備份保留數與成功後清除 | 保留上游 maxBackups 限制；尚未加入「成功載入後立即刪除所有快照」模式。 |
| [#71](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/71) Velocity 日誌橋接警告 | 保留上游 RollbackMonitor 的橋接相容處理。 |
| [#72](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/72) 手動編譯函式庫 | 保留上游 BuildLibrarySupport；外部私有函式庫仍需合法提供。 |
| [#74](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/74) 私有 GitHub release | 上游已有 API asset URL 與有範圍的憑證下載修正，回報者確認可用。 |
| [#76](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/76) updatePath 解析 | 上游已有命名解析，本次再補只指定 updatePath 也啟用暫存。 |
| [#77](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/77) Citizens 再次失效 | 同 #14，本次增加 HTML 備援。 |
| [#78](https://github.com/NewAmazingPVP/AutoUpdatePlugins/issues/78) Boar 抓舊版 | 實際查詢新版 loader=geyser、舊版為 velocity；擴充路徑可推斷並可明確覆寫，核心檢查通過。 |

## 驗證邊界

依使用者要求限縮必要檢查：本次交付的核心套件共 47 項通過，未重啟正式伺服器或壓測，也未承諾所有外部來源與所有插件可無條件更新。新的排程與來源搜尋為背景工作；候選名稱不明時交由管理員核對。
