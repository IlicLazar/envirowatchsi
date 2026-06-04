package ast

object AstPrinter {

    fun print(node: AstNode, indent: String = "") {
        when (node) {

            is ProgramNode -> {
                println("${indent}ProgramNode")
                node.cities.forEach {
                    print(it, "$indent  ")
                }
            }

            is CityNode -> {
                println("${indent}CityNode(name=\"${node.name}\")")
                node.items.forEach {
                    print(it, "$indent  ")
                }
            }

            is AreaNode -> {
                println("${indent}AreaNode(name=\"${node.name}\")")
            }

            is RiverNode -> {
                println("${indent}RiverNode(name=\"${node.name}\")")
            }

            is DateNode -> {
                println("${indent}DateNode(value=\"${node.value}\")")
            }

            is RuleNode -> {
                println("${indent}RuleNode(name=\"${node.name}\")")
                node.items.forEach {
                    print(it, "$indent  ")
                }
            }

            is ListNode -> {
                println("${indent}ListNode(name=\"${node.name}\")")
                node.values.forEach {
                    print(it, "$indent  ")
                }
            }

            is ForNode -> {
                println("${indent}ForNode(variable=${node.variable}, iterable=${node.iterable})")
                node.items.forEach {
                    print(it, "$indent  ")
                }
            }

            is IfNode -> {
                println("${indent}IfNode")
                print(node.condition, "$indent  ")
                println("${indent}  then")
                node.thenItems.forEach {
                    print(it, "$indent    ")
                }
                if (node.elseItems.isNotEmpty()) {
                    println("${indent}  else")
                    node.elseItems.forEach {
                        print(it, "$indent    ")
                    }
                }
            }

            is WhileNode -> {
                println("${indent}WhileNode")
                print(node.condition, "$indent  ")
                node.items.forEach {
                    print(it, "$indent  ")
                }
            }

            is ConditionNode -> {
                println("${indent}ConditionNode(operator=${node.operator})")
                print(node.left, "$indent  ")
                print(node.right, "$indent  ")
            }

            is StringValueNode -> {
                println("${indent}StringValueNode(value=\"${node.value}\")")
            }

            is NumberValueNode -> {
                println("${indent}NumberValueNode(value=${node.value})")
            }

            is IdentifierValueNode -> {
                println("${indent}IdentifierValueNode(value=${node.value})")
            }

            is PointValueNode -> {
                println("${indent}PointValueNode")
                print(node.value, "$indent  ")
            }

            is GenericStationNode -> {
                println("${indent}GenericStationNode")
                println("${indent}  name = ${node.name}")
                println("${indent}  type = ${node.type}")
                println("${indent}  location = (${node.location.longitude}, ${node.location.latitude})")
            }

            is AirStationNode -> {
                println("${indent}AirStationNode(name=\"${node.name}\")")
                println("${indent}  location = (${node.location.longitude}, ${node.location.latitude})")

                node.items.forEach {
                    print(it, "$indent    ")
                }
            }

            is MeteoStationNode -> {
                println("${indent}MeteoStationNode(name=\"${node.name}\")")
                println("${indent}  location = (${node.location.longitude}, ${node.location.latitude})")

                node.items.forEach {
                    print(it, "$indent    ")
                }
            }

            is HydroStationNode -> {
                println("${indent}HydroStationNode(name=\"${node.name}\")")
                println("${indent}  river = ${node.river}")
                println("${indent}  location = (${node.location.longitude}, ${node.location.latitude})")

                node.items.forEach {
                    print(it, "$indent    ")
                }
            }

            is SourceNode -> {
                println("${indent}SourceNode(value=\"${node.value}\")")
            }

            is StatusNode -> {
                println("${indent}StatusNode(value=\"${node.value}\")")
            }

            is PollutantNode -> {
                println("${indent}PollutantNode(type=\"${node.type}\", unit=\"${node.unit}\")")
            }

            is MeasurementNode -> {
                println("${indent}MeasurementNode(name=\"${node.name}\", value=${node.value})")
            }

            is AqiNode -> {
                println("${indent}AqiNode(value=${node.value})")
            }

            is WeatherMeasurementNode -> {
                println("${indent}WeatherMeasurementNode(type=${node.type}, value=${node.value}, unit=${node.unit})")
            }

            is WindMeasurementNode -> {
                println("${indent}WindMeasurementNode(speed=${node.speed}, direction=${node.direction})")
            }

            is HydroMeasurementNode -> {
                println("${indent}HydroMeasurementNode(type=${node.type}, value=${node.value}, unit=${node.unit})")
            }

            is ThresholdNode -> {
                println("${indent}ThresholdNode(parameter=${node.parameter}, warning=${node.warning}, critical=${node.critical})")
            }

            is FloodThresholdNode -> {
                println("${indent}FloodThresholdNode(warning=${node.warning}, critical=${node.critical})")
            }

            is IntervalNode -> {
                println("${indent}IntervalNode(from=${node.from}, to=${node.to}, step=${node.step})")

                node.measurements.forEach {
                    print(it, "$indent    ")
                }
            }

            is PointNode -> {
                println("${indent}PointNode(${node.longitude}, ${node.latitude})")
            }
        }
    }
}
