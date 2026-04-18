package com.example.evolve.effect

/**
 * 効果の発動トリガー（誘発タイミング）
 * ユーザーによって定義された多様なイベントに対応
 */
enum class EffectTrigger {
    Passive,   // これが場にある限り
    OnMainPhase,    // メインフェイズが来たとき
    OnLeaderHealed, //リーダーの体力が増加したとき
    OnLeaderDamaged, // リーダーの体力が減ったとき
    OnCardPlayed,   // カードをプレイしたとき
    OnSpellPlayed,  // スペルをプレイしたとき
    OnFollowerPlayed,// フォロワーをプレイしたとき
    OnAmuletPlayed,  // アミュレットをプレイしたとき
    OnCardEnteredField, // フォロワーかアミュレットが場に出たとき(ファンファーレ)
    OnCardSentToGrave,  // 墓場に置かれたとき
    OnCardLeftField,    // 場から離れたとき
    OnCardReturnedToHand,//手札に戻ったとき
    OnAbilityActivated, // 起動能力を使用したとき(進化含む)
    OnAttack,           // 攻撃時
    OnDamaged,          // ダメージを受けたとき
    OnCombatDamaged,    // 交戦ダメージを与えたとき
    OnActed,            // アクトしたとき
    OnDraw,             // 1枚引いたとき
    OnDriveCheck,       // ドライブチェックしたとき
    OnDriveTrigger,     // ドライブチェックによってトリガーしたとき
    OnEndPhase,         // エンドフェイズが来たとき
    OnDiscarded,        // 手札を捨てたとき
    OnBanishedFromHand  // 手札を消滅したとき
}