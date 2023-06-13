package ast

typealias ObjectType = String

interface Object {
    fun type(): ObjectType
    fun inspect(): String
}