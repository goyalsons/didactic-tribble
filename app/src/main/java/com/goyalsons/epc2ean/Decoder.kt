package com.goyalsons.epc2ean

object Decoder {
    private val PARTITION_TABLE = mapOf(
        0 to Quad(12,1,40,4),
        1 to Quad(11,2,37,7),
        2 to Quad(10,3,34,10),
        3 to Quad(9,4,30,14),
        4 to Quad(8,5,27,17),
        5 to Quad(7,6,24,20),
        6 to Quad(6,7,20,24)
    )
    data class Quad(val a:Int,val b:Int,val c:Int,val d:Int)
    data class Result(val epc:String,val gtin14:String?,val ean13:String?,val serial:String?,val status:String,val error:String?=null)
    private fun hexToBits(hex:String):String{
        val clean=hex.trim().removePrefix("0x").removePrefix("0X").replace(" ","")
        val bi=clean.toBigInteger(16)
        return bi.toString(2).padStart(clean.length*4,'0')
    }
    private fun mod10(number:String):Char{
        var total=0
        number.reversed().forEachIndexed{idx,ch-> val d=ch.digitToInt(); total+= if((idx+1)%2==1) d*3 else d }
        val cd=(10-(total%10))%10; return ('0'+cd)
    }
    fun decodeSgtin96(epcHexInput:String):Result{
        val epcHex=epcHexInput.trim().removePrefix("epc:").removePrefix("EPC:").removePrefix("0x").removePrefix("0X").replace(" ","")
        if(epcHex.isEmpty()) return Result(epcHexInput,null,null,null,"error","empty EPC")
        val bits=try{hexToBits(epcHex)}catch(e:Exception){return Result(epcHexInput,null,null,null,"error","invalid hex")}
        if(bits.length!=96) return Result(epcHexInput,null,null,null,"error","must be 96 bits (24 hex)")
        val header=bits.substring(0,8).toInt(2)
        if(header!=0x30) return Result(epcHexInput,null,null,null,"error","header!=0x30")
        val partition=bits.substring(11,14).toInt(2)
        val p=PARTITION_TABLE[partition]?:return Result(epcHexInput,null,null,null,"error","bad partition")
        val cpBin=bits.substring(14,14+p.c)
        val irBin=bits.substring(14+p.c,14+p.c+p.d)
        val serialBin=bits.substring(14+p.c+p.d)
        val company=cpBin.toBigInteger(2).toString().padStart(p.a,'0')
        val item=irBin.toBigInteger(2).toString().padStart(p.b,'0')
        val serial=serialBin.toBigInteger(2).toString()
        val ind=item.first(); val body=if(item.length>1)item.substring(1) else ""
        val base="$ind$company$body"
        if(base.any{!it.isDigit()}) return Result(epcHexInput,null,null,serial,"error","non-numeric")
        val gtin14=base+mod10(base)
        val ean13= if(ind=='0'){ val ean12=company+body; ean12+mod10(ean12)} else null
        return Result(epcHexInput,gtin14,ean13,serial,"ok")
    }
}
