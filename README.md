# Adler Life App

瞬間の手ざわりを静かに記録し、
その日のうちだけAIとの対話で振り返るための Android アプリです。

## 仕様サマリー
- **軌跡タブ**: 上部に過去ログ一覧、下部に常設フォームを置き、時間帯に関係なく何度でも記録できます。
- **対話タブ**: その日の日記録だけを材料に、評価しない問いかけをAIが返します。会話履歴は永続化しません。
- **暦タブ**: 月間カレンダー上で、その日の気分平均を色で表示します。日付タップで `ModalBottomSheet` に当日のログ一覧を表示します。

## アーキテクチャ
- **UI**: Jetpack Compose + Material 3
- **Presentation**: `AdlerViewModel`
- **Data**: Room (`TraceLog`, `AdlerDao`, `AdlerDatabase`)
- **Domain**: `AiCoach` / `HybridAiCoach`
- **Repository**: `AdlerRepository`

## OpenAI API
`app/build.gradle.kts` の `BuildConfig.OPENAI_API_KEY` にキーを設定すると、OpenAI Responses API を呼びます。
キー未設定時は、問いかけ調を維持したローカルフォールバックで動作します。
