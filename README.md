# Forest Mood

森の中でひと息つくように、
その日の気分や体調を静かに記録して振り返る Android アプリです。

## コンセプト
- メンタルヘルスを主軸に、毎日の気分・体調をシンプルに残す
- 評価しない / 比較しない / 強制しない
- 「今日どんな気分だった？」をやさしく言葉にする

## タブ構成
- **軌跡**: 気分・エネルギー・今日あったこと・今の気持ちを記録
- **対話**: 今日の記録だけを材料に、穏やかなAIの問いかけで振り返る
- **暦**: 月間カレンダー上で気分の平均を色で表示し、日付タップで詳細を確認
- **設定**: 免責事項、プライバシーポリシー、記録エクスポート、バージョン情報

## 追加機能
- 初回起動時の免責事項ダイアログ（SharedPreferencesで表示済み管理）
- アプリ内プライバシーポリシー表示
- ShareSheet経由のテキストエクスポート
- Google AdMob バナー広告
- スプラッシュスクリーンと森モチーフのアプリアイコン

## 技術構成
- Kotlin + Jetpack Compose + Material3
- MVVM (`AdlerViewModel`)
- Room (`TraceLog`)
- OpenAI Responses API + ローカルフォールバック
- SharedPreferences
- Google AdMob
- SplashScreen API
