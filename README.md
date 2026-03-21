# Adler Life App

アドラー心理学の「課題の分離」「自己受容」「いまここへの注目」を、
タスク管理ではなく**瞬間充実型のセルフリフレクション**として体験する Android アプリです。

## 体験設計の方針
- **正解を提示しない**: AIは命令ではなく「試してみる」提案や問いかけを返す。
- **強制しない**: 達成率・連続記録・ランキング・未完了表示を置かない。
- **比較させない**: 可視化は自分の軌跡のみ。スコアではなく行動カテゴリの色で示す。
- **評価しない**: ログは「何をしたか」「どう感じたか」だけを保存する。

## アーキテクチャ
- **UI**: Jetpack Compose + Material 3。1画面内のやさしいタブ切り替えで、圧を下げる。
- **Presentation**: `AdlerViewModel` が画面状態を一元管理し、入力・保存・AI呼び出しを仲介。
- **Data**: Room (`AdlerDatabase`, `AdlerDao`) に衝動ログ・行動ログ・振り返りログを保存。
- **Domain**: `AiCoach` がマイクロアクション提案と価値観フィードバックを担当。APIキー未設定時はローカル推論で動作。
- **Repository**: `AdlerRepository` が Flow を束ね、AI と DB を接続する。

## 主要画面
1. **今日の衝動ログ**: やりたいこと・気分・エネルギーを入力し、その瞬間に合う小さな行動を提案。
2. **行動の記録**: 達成判定なしで、行動内容と感情だけを記録。
3. **一日の振り返り**: 今日の出来事・印象的な瞬間・小さな喜びから、価値観の仮説を返す。
4. **AIコーチング**: 最近の傾向を問いかけとして言語化。
5. **軌跡カレンダー**: 日付ごとの行動カテゴリを色の点で表示。

## OpenAI API の使い方
`app/build.gradle.kts` の `BuildConfig.OPENAI_API_KEY` にキーを設定すると、`HybridAiCoach` が Responses API を利用します。
未設定時は、UX思想を壊さないローカルルールベースの返答へ自動でフォールバックします。

## 実装の読みどころ
- `ui/screens/AdlerLifeApp.kt`: 主要画面の Compose UI。
- `viewmodel/AdlerViewModel.kt`: MVVM の状態管理。
- `data/model/Models.kt`: Room Entity とUI用モデル。
- `domain/AiCoach.kt`: AI連携とローカルフォールバック。
