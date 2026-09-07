# Mouse Android

PC版ShareOneMouseから受信したマウス操作を、Android端末上のカーソル移動、タップ、スワイプ、スクロールとして実行するアプリです。

Android版はUDP/TCPのServerとして動作します。接続するPCではShareOneMouseを起動し、`Client` を選択してください。

## 動作環境

- Android 8.0（API 26）以上
- 同じLANに接続されたWindows PC
- PC版ShareOneMouse
- TCP/UDPポート `5000` を利用できるネットワーク
- AndroidのAccessibility Service

現在のビルド設定は次のとおりです。

| 項目 | 値 |
| --- | --- |
| Application ID | `com.momos.mouseandroid` |
| Minimum SDK | 26 |
| Target SDK | 36 |
| Compile SDK | 36 |

## 初回設定

マウス操作をAndroid上のジェスチャーとして実行するため、Accessibility Serviceを有効にする必要があります。

1. アプリを起動します。
2. 表示された案内で「設定を開く」を選択します。
3. Androidのアクセシビリティ設定を開きます。
4. 「ダウンロードしたアプリ」を選択します。
5. 「Mouse Android操作サービス」を選択します。
6. 「サービスを使用」を有効にします。
7. アプリへ戻ります。

アプリ起動時と設定画面から戻った時に有効状態を再確認します。すでに有効な場合、設定を促すダイアログは表示されません。

## 接続方法

1. Android端末とPCを同じLANへ接続します。
2. Android版を起動し、Accessibility Serviceが有効であることを確認します。
3. Android版で「検索」を押します。
4. PC版ShareOneMouseを起動します。
5. PC版で `Client` を選択します。
6. PCがAndroidを検出するとTCP接続が確立されます。
7. PCでカーソルを移行させる画面端を選択します。
8. 選択した端へPCカーソルを移動すると、Androidへ操作が移行します。

PC版の現在の役割では、`Client` が操作送信側、`Server` が操作受信側です。Android版は受信側ですがネットワーク上ではServerとして待ち受けるため、PC側では必ず `Client` を選択します。

## 対応している操作

| PCから受信する操作 | Android側の動作 | 状況 |
| --- | --- | --- |
| マウス移動 | Accessibility Overlayのカーソルを移動 | 実装済み |
| 左クリック | タップ | 実装済み |
| 左ドラッグ | スワイプ | 実装済み |
| 右クリック | 長押し | 実装済み |
| ホイール | 縦スクロール | 実装済み |
| 中央クリック | なし | 未実装 |
| Keyboard入力 | なし | 未実装 |

Androidカーソルが反対側の画面端へ到達すると、`TOUCH_WALL` をPCへ返して操作をPC側へ戻します。

## 通信方式

接続には次の2種類の通信を利用します。

- UDP: PCからAndroid Serverを探索
- TCP: JSON形式の操作データを双方向に送受信

通信データの大分類は次のとおりです。

- `MOUSE`
- `KEYBOARD`
- `WALL_TYPE`
- `SYSTEM_EXIT`

マウスイベントはPC版と同じ名前に統一されています。

- `MOVE`
- `LEFT_CLICK`
- `RIGHT_CLICK`
- `WHEEL_CLICK`
- `DRAG`
- `WHEEL_MOVE`
- `TOUCH_WALL`
- `SEND_MOUSE`
- `CLOSE_INVISIBLE_WINDOW`

## 主な構成

| ファイル | 役割 |
| --- | --- |
| `MainActivity.kt` | アプリ起動とAccessibility Serviceの状態確認 |
| `MouseAppUI.kt` | 接続状態、検索・切断ボタンのUI |
| `MouseViewModel.kt` | UI、通信、マウス処理の連携 |
| `ConnectionRepository.kt` | UDP ServerとTCP Serverの管理 |
| `UdpServer.kt` | PCからの探索要求を受信して応答 |
| `TcpServer.kt` | TCP接続とJSONデータの送受信 |
| `MouseCommandProcessor.kt` | マウスイベントの解析、座標管理、PCへの復帰通知 |
| `MouseAccessibilityService.kt` | カーソル表示、タップ、スワイプ、スクロールの実行 |
| `JsonConverter.kt` | PCへ返すデータのJSON変換 |

処理の流れは次のとおりです。

```text
PC Client
   ↓ UDP探索
Android UdpServer
   ↓ TCP接続
Android TcpServer
   ↓ JSON変換
MouseViewModel
   ↓
MouseCommandProcessor
   ↓
MouseAccessibilityService
```

## 現在の完成状況

| 機能 | 状況 |
| --- | --- |
| Accessibility Serviceの登録 | 実装済み |
| 起動・復帰時の有効状態判定 | 実装済み |
| UDP Serverによる探索応答 | 実装済み |
| TCP Serverによる接続 | 実装済み |
| PC版と共通のJSONイベント名 | 実装済み |
| Android上のカーソル表示 | 実装済み |
| タップ・長押し・スワイプ | 実装済み |
| ホイールによるスクロール | 実装済み |
| PCへのカーソル復帰 | 実装済み・実機確認継続中 |
| 画面回転時のサイズ更新 | 実装済み |
| Keyboard入力 | 未実装 |
| 自動再接続 | 未実装 |
| 接続認証・暗号化 | 未実装 |
| 自動テスト | 未実装 |

## 既知の制約

- 接続先の認証と通信の暗号化は実装されていません。信頼できるLAN内で利用してください。
- 不正なJSONや受信例外が発生した場合、TCP接続を閉じます。
- `SYSTEM_EXIT` を受信した場合のAndroid側処理は未実装です。
- 中央クリックとKeyboard入力は現在処理されません。
- Android端末やOSの省電力設定によって、バックグラウンド時に通信が停止する可能性があります。
- 端末メーカーやAndroidバージョンによって、アクセシビリティ設定画面の名称が異なる場合があります。

## Keyboard入力の今後の予定

現在は `DataType.KEYBOARD` の列挙値のみ存在し、受信時は未定義イベントとして処理しています。

今後は次の順序で実装します。

1. Accessibility Serviceでフォーカス中の編集可能な入力欄を検出します。
2. 入力可能になったことを `INPUT_ENABLE` としてPCへ送信します。
3. PCから変換中文字列を `COMPOSITION_UPDATE` として受信します。
4. 変換中文字列をAndroid上のオーバーレイへ表示します。
5. 確定文字列を `COMMIT` として受信します。
6. Accessibility APIの `ACTION_SET_TEXT` などで入力欄へ反映します。
7. 入力欄からフォーカスが外れた場合は `INPUT_DISABLE` を送信します。

Keyboard対応では、Accessibility Serviceの設定でウィンドウ内容を取得できるようにする変更と、個人情報を扱う入力欄を転送対象にしないための制御が必要です。

## ビルド方法

Android Studioでこのプロジェクトを開くか、プロジェクト直下でGradle Wrapperを実行します。

Kotlinのコンパイル確認:

```powershell
.\gradlew.bat compileDebugKotlin
```

デバッグAPKの作成:

```powershell
.\gradlew.bat assembleDebug
```

生成されたAPKは通常、次の場所に出力されます。

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 開発時の注意

- `.gradle/`、`build/`、`.idea`のローカルキャッシュはコミットしないでください。
- PC版とAndroid版で `DataType`、`MouseEventType`、`WallType` の名前を一致させてください。
- ネットワークイベントを追加する場合は、送信側と受信側を同時に更新してください。
- Accessibility Serviceを変更した後は、端末側でサービスを一度無効化して再度有効化すると反映を確認しやすくなります。
