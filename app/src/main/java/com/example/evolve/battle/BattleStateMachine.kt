    package com.example.evolve.battle

    import com.example.evolve.effect.StackedEffect
    import com.example.evolve.effect.CardEffectLoader
    import com.example.evolve.effect.EffectTrigger
    import com.example.evolve.model.CardData
    import android.content.Context

    class BattleStateMachine(private var _state: BattleState, private val context: Context) {
        val state: BattleState
            get() = _state

        fun startTurn() {
            startTurnPhase()
            performCheckTiming() // ✅ チェックタイミング
            mainPhaseLoop()
            performCheckTiming()
            endPhase()
            performCheckTiming()
            endTurnPhase()
        }

        fun startTurnPhase() {
            val current = getTurnPlayer()

            if (current.maxPP < 10) current.maxPP++
            current.currentPP = current.maxPP

            if (state.phase == Phase.START && current.ep == 0) {
                if (state.turnPlayer == state.player2.name) {
                    current.ep = 3
                }
            }

            if (!(_state.turnPlayer == _state.player1.name && _state.phase == Phase.START)) {
                drawCard(current)
            }

            checkTiming(EffectTrigger.OnMainPhase)
            _state.phase = Phase.MAIN
        }

        fun mainPhaseLoop() {
            checkTiming(EffectTrigger.OnMainPhase)
            _state.phase = Phase.END
        }

        fun endPhase() {
            val current = getTurnPlayer()

            checkTiming(EffectTrigger.OnEndPhase)

            while (current.hand.size > 7) {
                val discarded = current.hand.removeAt(0)
                current.graveyard.add(discarded)
                checkTiming(EffectTrigger.OnDiscarded)
            }

            checkTiming(EffectTrigger.OnEndPhase)
        }

        fun endTurnPhase() {
            _state.turnPlayer = getOpponent().name
            _state.phase = Phase.START
        }

        fun drawCard(player: PlayerState) {
            if (player.deck.isEmpty()) {
                player.leaderHp = 0
            } else {
                val card = player.deck.removeAt(0)
                if (player.hand.size < 7) {
                    player.hand.add(card)
                } else {
                    player.graveyard.add(card)
                }
                checkTiming(EffectTrigger.OnDraw)
            }
        }

        fun playCard(card: CardData) {
            val current = getTurnPlayer()
            current.hand.remove(card)
            current.field.add(card)

            checkTiming(EffectTrigger.OnCardPlayed)

            when {
                card.kind.contains("フォロワー") -> checkTiming(EffectTrigger.OnFollowerPlayed)
                card.kind.contains("アミュレット") -> checkTiming(EffectTrigger.OnAmuletPlayed)
            }

            checkTiming(EffectTrigger.OnCardEnteredField)
        }

        fun evolveCard(card: CardData) {
            val current = getTurnPlayer()
            current.field.remove(card)
            // 進化後カードを探して場に出す処理など…
            checkTiming(EffectTrigger.OnAbilityActivated)
        }

        fun attackWith(card: CardData) {
            val current = getTurnPlayer()
            checkTiming(EffectTrigger.OnAttack)
        }

        fun dealLeaderDamage(player: PlayerState, amount: Int) {
            player.leaderHp -= amount
            if (player.leaderHp < 0) player.leaderHp = 0
            checkTiming(EffectTrigger.OnLeaderDamaged)
        }

        fun healLeader(player: PlayerState, amount: Int) {
            player.leaderHp += amount
            checkTiming(EffectTrigger.OnLeaderHealed)
        }

        private fun checkTiming(trigger: EffectTrigger) {
            val allCards = _state.player1.field + _state.player2.field

            for (card in allCards) {
                val effects = CardEffectLoader.loadEffectsForCardByTrigger(context, card.card, card.expansion, trigger)
                if (effects.isNotEmpty()) {
                    val owner = if (_state.player1.field.contains(card)) _state.player1.name else _state.player2.name
                    _state.stack.add(
                        StackedEffect(
                            sourceCardId = card.card,
                            owner = owner,
                            effects = effects
                        )
                    )
                }
            }
        }

        private fun performCheckTiming() {
            _state = com.example.evolve.effect.StackedEffectHandler.resolveAll(state)
        }

        private fun getTurnPlayer(): PlayerState {
            return if (_state.turnPlayer == _state.player1.name) _state.player1 else _state.player2
        }

        private fun getOpponent(): PlayerState {
            return if (_state.turnPlayer == _state.player1.name) _state.player2 else _state.player1
        }
    }