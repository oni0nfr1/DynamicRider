package io.github.oni0nfr1.dynamicrider.client.command

import io.github.oni0nfr1.dynamicrider.client.command.debug.CommandVar

/**
 * 디버그용 객체입니다.
 *
 * HUD 요소 디자인을 조정할 때 사용하는 기능으로,
 * 여기에 아래와 같이 값을 넣어두면
 * CommandVar 어노테이션의 name값을 통해 /debugvalue 커맨드로 런타임에 값을 수정할 수 있습니다
 */
object DebugVariables {
    @field:CommandVar("example")
    var example: Int = 1
    @field:CommandVar("team_boost_gauge")
    var teamBoostGauge: Double = 0.0
    @field:CommandVar("exp_gauge")
    var expGauge: Double = 0.0
}