package ast

sealed interface AstNode

data class ProgramNode(
    val cities: List<CityNode>
) : AstNode

data class CityNode(
    val name: String,
    val items: List<CityItemNode>
) : AstNode

sealed interface CityItemNode : AstNode

data class AreaNode(
    val name: String,
    val points: List<PointNode>
) : CityItemNode

data class RiverNode(
    val name: String
) : CityItemNode

data class DateNode(
    val value: String
) : CityItemNode

data class RuleNode(
    val name: String,
    val items: List<RuleItemNode>
) : CityItemNode

sealed interface RuleItemNode : AstNode

sealed interface StationNode : CityItemNode

data class GenericStationNode(
    val name: String,
    val type: String,
    val location: PointNode
) : StationNode

data class AirStationNode(
    val name: String,
    val location: PointNode,
    val items: List<AirItemNode>
) : StationNode

data class MeteoStationNode(
    val name: String,
    val location: PointNode,
    val items: List<MeteoItemNode>
) : StationNode

data class HydroStationNode(
    val name: String,
    val river: String,
    val location: PointNode,
    val items: List<HydroItemNode>
) : StationNode

sealed interface AirItemNode : AstNode
sealed interface MeteoItemNode : AstNode
sealed interface HydroItemNode : AstNode

data class SourceNode(val value: String) : AirItemNode, MeteoItemNode, HydroItemNode
data class StatusNode(val value: String) : AirItemNode, MeteoItemNode, HydroItemNode

data class PollutantNode(
    val type: String,
    val unit: String
) : AirItemNode

data class MeasurementNode(
    val name: String,
    val value: String,
    val unit: String?,
    val time: DateTimeNode?
) : AirItemNode, MeteoItemNode, HydroItemNode

data class AqiNode(
    val value: String,
    val time: DateTimeNode?
) : AirItemNode

data class WeatherMeasurementNode(
    val type: String,
    val value: String,
    val unit: String?,
    val time: DateTimeNode?
) : MeteoItemNode

data class WindMeasurementNode(
    val speed: String,
    val direction: String,
    val time: DateTimeNode?
) : MeteoItemNode

data class HydroMeasurementNode(
    val type: String,
    val value: String,
    val unit: String?,
    val time: DateTimeNode?
) : HydroItemNode

data class ThresholdNode(
    val parameter: String,
    val warning: String,
    val critical: String
) : AirItemNode, RuleItemNode

data class FloodThresholdNode(
    val warning: String,
    val critical: String
) : HydroItemNode, RuleItemNode

data class IntervalNode(
    val from: DateTimeNode,
    val to: DateTimeNode,
    val step: String,
    val measurements: List<AstNode>
) : AirItemNode, MeteoItemNode, HydroItemNode

data class DateTimeNode(
    val value: String
) : AstNode

data class PointNode(
    val longitude: String,
    val latitude: String
) : AstNode
