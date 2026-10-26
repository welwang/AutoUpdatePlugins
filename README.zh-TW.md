# AutoUpdatePlugins 繁體中文客製版

版本：`12.3.0-tw.1`。以 NewAmazingPVP/AutoUpdatePlugins 的 MIT 原始碼修改，保留原作者與授權。

## 安裝

1. 停服後，將 Release 內的 `autoupdateplugins-12.3.0-tw.1.jar` 放入 `plugins/`，移除舊版 AutoUpdatePlugins JAR，保留設定資料夾。
2. 啟動伺服器。新安裝預設每天 **台北時間 04:00** 尋找與下載更新，不會每次開機立刻更新。
3. `/aup discover` 預覽來源，`/aup discover apply` 儲存可確認的來源。直接 `/aup update` 也會先辨識來源再更新。
4. 更新檔預設放入 `plugins/update/`，**重啟後套用**。預設不會自動重啟。

Spigot/Paper/Purpur/Folia 使用 `/aup`；Velocity 使用 `/vaup`；BungeeCord/Waterfall 使用 `/baup`。管理權限：`autoupdateplugins.manage`（預設 OP）。

既有設定的時區、cron 與其他已存在的值會保留。升級後請用指令明確設定時間。如果舊清單有指向原作者倉庫的 AutoUpdatePlugins 自我更新項目，請移除或停用，避免將客製版換回上游版。

## 指令

```text
/aup schedule timezone Asia/Taipei
/aup schedule daily 04:00
/aup schedule daily 04:00 16:00
/aup schedule status
/aup schedule off
/aup schedule on
/aup schedule cron 30 3 * * 1-5
/aup update
/aup update LuckPerms
/aup check
/aup pending
/aup discover
/aup discover apply
/aup reload
/aup log
/aup stop
```

`daily` 可設定多個 24 小時制 `HH:mm` 時刻，立即儲存並替換舊排程。`status` 顯示下次排程時間。`off` 關閉自動排程，仍可手動更新。`update` 準備安裝更新；`check` 僅檢查。停服期間錯過的時刻不補跑。設定錯誤時停用該排程並顯示原因。

## 設定

設定檔：`plugins/AutoUpdatePlugins/config.yml`。以下為需調整的部分，保留完整設定檔的其他選項：

```yaml
updates:
  interval: 120
  bootTime: 50
  schedule:
    enabled: true
    timezone: "Asia/Taipei"
    times: ["04:00", "16:00"]
    cron: ""
behavior:
  useUpdateFolder: true
  installMissing: false
  manualMode: false
  ignoreDuplicates: true
  restartAfterUpdate: false
discovery:
  autoAdd: true
metadata:
  enabled: true
  skipDownloadWhenUnchanged: true
```

優先順序：**cron → times → interval**。使用舊的間隔模式時，設定 `times: []`、清空 `cron`，再 `/aup reload`。

## 智慧尋找來源

已設定來源時，由官方 API 或下載頁解析最新可用檔案，沿用上游多來源支援。未設定來源時：

1. 以已安裝 JAR 的 SHA-1 向 Modrinth 精確查詢專案，保留辨識到的平台。
2. 無精確結果時，辨識插件內 `website`／`url` 的 GitHub、Modrinth、Hangar、Spigot 專案網址。
3. 仍無法確認時，搜尋 Modrinth 並顯示候選與作者；不會僅憑同名自動加入。其他網站可自行指定官方來源。

預設在排程及手動「更新全部」前儲存可確認的來源。`discovery.autoAdd: false` 可只使用現有清單。已有來源及停用項目均保留；客製版自身不會自動加入上游來源。初次辨識傳送 SHA-1 查詢值，不上傳 JAR 內容；無法確認時才以插件名稱搜尋。

## 無新版就跳過

支援版本資訊的來源先比較快取，相同來源版本不再下載大型 JAR。第一次接管既有插件、快取失效或沒有版本 API 的來源，可能需要下載一次比較雜湊；內容相同仍跳過安裝，不觸發重啟。

Minecraft 相容性、平台與版本政策仍有效；「最新」表示符合條件的最新版本。付費來源仍需平台授權。

## 清單範例與問題處理

`plugins/AutoUpdatePlugins/list.yml`：

```yaml
LuckPerms: "https://modrinth.com/plugin/luckperms"
Citizens: "https://ci.citizensnpcs.co/job/Citizens2/"
Boar: "https://modrinth.com/plugin/boar?latest=true&loader=geyser | filePath=plugins/Geyser-Velocity/extensions | updatePath=plugins/Geyser-Velocity/extensions/update | useUpdateFolder=true"
# SomePlugin: "https://github.com/Author/SomePlugin"
```

- 缺少／刪除的插件預設跳過。若已有待套用更新檔，刪除插件時也需刪掉該插件在 update 資料夾的檔案，避免伺服器啟動時自行套用。
- 刻意安裝新插件：網址加 `?installMissing=true`，已有其他參數則用 `&installMissing=true`。`force=true` 不會略過這項設定或身分比對。
- `updatePath=...` 明確啟用暫存更新；另設 `useUpdateFolder=false` 時以明確設定優先。
- Geyser 擴充路徑可推斷 `geyser` 平台；明確的 `loader=...` 優先。
- 無法確認的來源可核對作者後 `/aup add <插件名稱> <官方來源網址>`。
- 下載檔與原插件身分不符會阻擋覆寫。插件改名需自行核對並遷移。
- 自動來源辨識限標準插件描述檔；無描述檔的擴充請手動設定來源與路徑。

## 驗證與編譯

47 項核心檢查通過：排程、來源辨識、版本快取跳過下載、手動檢查／安裝、缺少插件跳過、錯誤身分阻擋、Jenkins 備援及 Geyser 選版。未進行正式伺服器重啟、壓力測試或所有外部插件的實機相容性測試。

以 Java 21、Maven 3.9.11 編譯，沿用 Java 8 位元碼目標；伺服器仍需其版本要求的 Java。GitHub 建置為手動觸發，原有測試均保留。

```text
mvn -B package -DskipTests
```

全部問題分類見 [FEEDBACK.zh-TW.md](FEEDBACK.zh-TW.md)。
