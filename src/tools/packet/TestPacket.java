package tools.packet;

import handling.MaplePacket;
import client.MapleCharacter;
import tools.HexTool;
import tools.data.output.MaplePacketLittleEndianWriter;

public class TestPacket {

	public static MaplePacket Test1() { // fairy pendant packet
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(0x68); // header
		mplew.writeInt(21); // 15 00 00 00 ??
		mplew.writeInt(0);
		mplew.writeInt(10); // 0A 00 00 00 %?
		//mplew.write(HexTool.getByteArrayFromHexString("15"));

		return mplew.getPacket();
	}
        public static MaplePacket Unk1() { // Unknown packet //登入後，伺服器列表前
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                //98 00 00
		mplew.writeShort(0x98); // header
		mplew.write(0); 

		return mplew.getPacket();
	}
        
        public static MaplePacket Unk2() { // Unknown packet //getCharInfo後
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                //0B 00 00 00 00 00 00
		mplew.writeShort(0x0B); // header
                mplew.writeInt(0);
		mplew.write(0); 

		return mplew.getPacket();
	}
        
        public static MaplePacket Unk3() { // Unknown packet //getCharInfo後
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                //56 00 03 00 13 00
		mplew.writeShort(0x56); // header
                mplew.writeShort(5);
		mplew.writeShort(0x0D); 

		return mplew.getPacket();
	}
        
        public static MaplePacket Unk4() { // Unknown packet //getCharInfo後
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                //AB 00
		mplew.writeShort(0xAB); // header

		return mplew.getPacket();
	}
        
        public static MaplePacket Unk5() { // Unknown packet //getCharInfo後
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                //A2 00
		mplew.writeShort(0xA2); // header

		return mplew.getPacket();
	}
        
        public static MaplePacket Unk6(final MapleCharacter chr)  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                
                mplew.writeShort(0x30); // header
                mplew.writeInt(chr.getId());
                mplew.write(1);
                mplew.writeZeroBytes(12);
                
                return mplew.getPacket();
        }
        public static MaplePacket Unk6_1(final MapleCharacter chr)  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                
                mplew.writeShort(0x1E5); // header
                mplew.write(1);
                
                return mplew.getPacket();
        }
        public static MaplePacket Unk7()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                
                mplew.writeShort(0xB3); // header
                mplew.write(HexTool.getByteArrayFromHexString("01 2A 00 00 00 52 00 00 00 47 00 00 00 49 00 00 00 1D 00 00 00 53 00 00 00 4F 00 00 00 51 00 00 00"));
       
                return mplew.getPacket();
        }
        public static MaplePacket Unk7_1(final MapleCharacter chr)  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                
                mplew.writeShort(0x1E6); // header
                mplew.writeInt(0);
                
                return mplew.getPacket();
        }
        
        public static MaplePacket Unk8()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("92 00 00"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk8_1(final MapleCharacter chr)  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                
                mplew.writeShort(0x1E7); // header
                mplew.writeInt(0);
                
                return mplew.getPacket();
        }
        public static MaplePacket Unk10()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("2E 00 00 00"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk11()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("2F 00 01"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk12()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("A5 00 44 00 80 05 BB 46 E6 17 02 FF"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk13()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("50 01 E8 02 00 00"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk13_1(final MapleCharacter chr)  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                
                mplew.writeShort(0xBA); // header
                mplew.writeInt(chr.getId());
                mplew.write(HexTool.getByteArrayFromHexString("01 08 00 A7 BE AA D1 BE 69 C0 6E 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 B6 D6 01 00 00 00 00 00 00 00 00 00 00 00 01 B6 D6 01 00 00 00 00 00 00 00 00 00 00 00 01 B6 D6 01 00 00 00 00 00 00 00 00 00 00 00 01 B6 D6 01 00 00 00 00 00 00 00 00 00 01 B6 D6 01 00 00 B5 4F D3 8C 00 00 00 00 00 00 00 00 00 00 01 B6 D6 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 B6 D6 01 00 00 00 00 00 00 00 00 00 00 00 01 B6 D6 01 00 00 00 00 00 84 4E 00 00 00 00 00 00 00 4B 75 00 00 05 8A DE 0F 00 06 A6 2C 10 00 07 85 5B 10 00 0B F0 DD 13 00 FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 A7 FF C3 01 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
                
                return mplew.getPacket();
        }
        
        //PARTY_OPERATION 
        public static MaplePacket Unk14()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("3B 00 07 00 00 00 00 99 42 17 00 08 27 17 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 EE EE 6F A4 54 A4 43 00 00 00 00 00 00 00 00 A4 FB A5 A4 A5 5B A4 FB 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 8A 0C 00 00 EE 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 25 00 00 00 37 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 00 00 00 07 00 00 00 FE FF FF FF FE FF FF FF FE FF FF FF FE FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 99 42 17 00 AE 45 24 06 AE 45 24 06 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF C9 9A 3B FF C9 9A 3B 00 00 00 00 20 20 30 2E 30 30 2C 20 FF C9 9A 3B FF C9 9A 3B 00 00 00 00 A0 9A 15 21 00 0B 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 00 00 FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
       
                return mplew.getPacket();
        }
        
        //TEMP_STATS_RESET 
        public static MaplePacket Unk15()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("23 00"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk16()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("5B 00 0A 00 6B 69 6C 6C 5F 63 6F 75 6E 74 01 00 30"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk16_1()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("1F 00 00 00 00 00 00 01 01 00"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk16_2()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("A1 00 03 14 00 6D 61 70 6C 65 6D 61 70 2F 65 6E 74 65 72 2F 31 30 30 30 30"));
                return mplew.getPacket();
        }
        
        public static MaplePacket Unk17()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("42 00 FF C9 9A 3B FF C9 9A 3B"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk18()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("4E 00 00"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk18_1()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("65 00 07 00 00 00 00 2C 01 00 00 01 00 00 00 12 00 A5 DF A8 E8 B2 BE B0 CA A6 DC AE 61 B1 DA A6 A8 AD FB 30 00 5B B9 EF B6 48 5D 20 A6 DB A4 76 5C 6E 5B AE C4 AA 47 5D 20 B2 BE B0 CA A6 DC B7 51 AD 6E AA BA AE 61 B1 DA A6 A8 AD FB A9 D2 A6 62 A6 61 C2 49 01 F4 01 00 00 01 00 00 00 10 00 A5 DF A8 E8 A5 6C B3 EA AE 61 B1 DA A6 A8 AD FB 47 00 5B B9 EF B6 48 5D 20 AE 61 B1 DA A6 A8 AD FB 31 A6 57 5C 6E 5B AE C4 AA 47 5D 20 A5 69 A5 48 B1 4E B7 51 A5 6C B3 EA AA BA AE 61 B1 DA A6 A8 AD FB A5 6C B3 EA A8 EC A6 DB A4 76 A9 D2 A6 62 AA BA A6 61 B9 CF A1 43 02 BC 02 00 00 01 00 00 00 17 00 A7 DA AA BA B1 BC C4 5F B2 76 31 2E 32 AD BF 28 31 35 A4 C0 C4 C1 29 6F 00 5B B9 EF B6 48 5D 20 A6 DB A4 76 5C 6E 5B AB F9 C4 F2 AE C9 B6 A1 5D 20 31 35 A4 C0 C4 C1 5C 6E 5B AE C4 AA 47 5D 20 C2 79 AE B7 A9 C7 AA AB AA BA B1 BC C4 5F B2 76 B4 A3 A4 C9 A6 DC 20 23 63 20 31 2E 32 AD BF 23 A1 43 5C 6E A1 B0 BB 50 B1 BC C4 5F AD C8 AC A1 B0 CA AD AB C5 7C AE C9 A1 41 A8 E4 AE C4 AA 47 B1 4E B5 4C AE C4 A1 43 03 84 03 00 00 01 00 00 00 17 00 A7 DA AA BA B8 67 C5 E7 AD C8 31 2E 32 AD BF 28 31 35 A4 C0 C4 C1 29 7D 00 5B B9 EF B6 48 5D 20 A6 DB A4 76 5C 6E 5B AB F9 C4 F2 AE C9 B6 A1 5D 20 31 35 A4 C0 C4 C1 5C 6E 5B AE C4 AA 47 5D C2 79 AE B7 A9 C7 AA AB AE C9 A1 41 B1 4E A6 DB A4 76 A8 FA B1 6F AA BA B8 67 C5 E7 AD C8 B4 A3 A4 C9 A6 DC 20 23 63 20 31 2E 32 AD BF 23 A1 43 5C 6E A1 B0 BB 50 B8 67 C5 E7 AD C8 AC A1 B0 CA AD AB C5 7C AE C9 A1 41 A8 E4 AE C4 AA 47 B1 4E B5 4C AE C4 A1 43 20 02 DC 05 00 00 01 00 00 00 17 00 A7 DA AA BA B1 BC C4 5F B2 76 31 2E 32 AD BF 28 33 30 A4 C0 C4 C1 29 6F 00 5B B9 EF B6 48 5D 20 A6 DB A4 76 5C 6E 5B AB F9 C4 F2 AE C9 B6 A1 5D 20 33 30 A4 C0 C4 C1 5C 6E 5B AE C4 AA 47 5D 20 C2 79 AE B7 A9 C7 AA AB AA BA B1 BC C4 5F B2 76 B4 A3 A4 C9 A6 DC 20 23 63 20 31 2E 32 AD BF 23 A1 43 5C 6E A1 B0 BB 50 B1 BC C4 5F AD C8 AC A1 B0 CA AD AB C5 7C AE C9 A1 41 A8 E4 AE C4 AA 47 B1 4E B5 4C AE C4 A1 43 03 D0 07 00 00 01 00 00 00 17 00 A7 DA AA BA B8 67 C5 E7 AD C8 31 2E 32 AD BF 28 33 30 A4 C0 C4 C1 29 7D 00 5B B9 EF B6 48 5D 20 A6 DB A4 76 5C 6E 5B AB F9 C4 F2 AE C9 B6 A1 5D 20 33 30 A4 C0 C4 C1 5C 6E 5B AE C4 AA 47 5D C2 79 AE B7 A9 C7 AA AB AE C9 A1 41 B1 4E A6 DB A4 76 A8 FA B1 6F AA BA B8 67 C5 E7 AD C8 B4 A3 A4 C9 A6 DC 20 23 63 20 31 2E 32 AD BF 23 A1 43 5C 6E A1 B0 BB 50 B8 67 C5 E7 AD C8 AC A1 B0 CA AD AB C5 7C AE C9 A1 41 A8 E4 AE C4 AA 47 B1 4E B5 4C AE C4 A1 43 20 04 B8 0B 00 00 01 00 00 00 16 00 AE 61 B1 DA A6 A8 AD FB AA BA B9 CE B5 B2 28 33 30 A4 C0 C4 C1 29 8C 00 5B B5 6F B0 CA B1 F8 A5 F3 5D 20 AE 61 A8 74 B9 CF A4 BA 36 A6 EC A5 48 A4 57 AA BA AE 61 B1 DA A6 A8 AD FB B5 6E A4 4A 5C 6E 5B AB F9 C4 F2 AE C9 B6 A1 5D 20 33 30 A4 C0 C4 C1 5C 6E 5B AE C4 AA 47 5D 20 B1 BC C4 5F B2 76 A9 4D B8 67 C5 E7 AD C8 B4 A3 A4 C9 20 23 63 31 2E 35 AD BF 23 20 A1 B0 B8 67 C5 E7 AD C8 BB 50 B1 BC C4 5F B2 76 20 AC A1 B0 CA AD AB C5 7C AE C9 A1 41 A8 E4 AE C4 AA 47 B1 4E B5 4C AE C4 A1 43 20"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk19()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("6D 00 00"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk20()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("7B 00 00 00 00 00 00 00 00 00 00 00 00"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk21()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("32 01 02"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk22()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("60 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk23()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("3D 00 3A"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk23_1()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("3E 00 07 00"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk24()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("40 00 20 00"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk24_1()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("91 00 02 0A 00 42 00 68 74 74 70 3A 2F 2F 74 77 2E 73 6E 73 2E 6D 61 70 6C 65 73 74 6F 72 79 2E 62 65 61 6E 66 75 6E 2E 63 6F 6D 2F 53 4E 53 5F 52 65 61 6C 2F 4D 61 70 6C 65 46 65 65 64 41 75 74 68 65 6E 2E 61 73 70 78 42 00 68 74 74 70 3A 2F 2F 74 77 2E 73 6E 73 2E 6D 61 70 6C 65 73 74 6F 72 79 2E 62 65 61 6E 66 75 6E 2E 63 6F 6D 2F 53 4E 53 5F 52 65 61 6C 2F 4D 61 70 6C 65 46 65 65 64 41 75 74 68 65 6E 2E 61 73 70 78 11 00 4D 61 70 6C 65 73 74 6F 72 79 20 47 6C 6F 62 61 6C 28 00 AC B0 A4 46 B8 F2 A6 6E A4 CD A4 C0 A8 C9 B3 DF AE AE A1 41 BB DD AD 6E A5 FD B6 69 A6 E6 B1 62 B8 B9 C5 E7 C3 D2 A1 43 14 00 C5 E7 C3 D2 A5 A2 B1 D1 A1 41 A6 41 B8 D5 B8 D5 AC DD A1 43 0A 00 C5 E7 C3 D2 A6 A8 A5 5C A1 43 19 00 A6 56 23 53 4E 53 5F 4C 6F 67 69 6E 49 44 23 A6 50 A8 42 A4 C6 A4 A4 A1 43 20 00 68 74 74 70 3A 2F 2F 74 77 2E 62 65 61 6E 66 75 6E 2E 63 6F 6D 2F 6D 61 70 6C 65 73 74 6F 72 79 08 00 B7 73 B7 AC A4 A7 A8 A6 20 00 68 74 74 70 3A 2F 2F 74 77 2E 62 65 61 6E 66 75 6E 2E 63 6F 6D 2F 6D 61 70 6C 65 73 74 6F 72 79 08 00 B7 73 B7 AC A4 A7 A8 A6 1D 00 B8 F2 23 4C 49 4E 4B 45 44 5F 53 4E 53 5F 4E 41 4D 45 23 A6 50 A8 42 A4 C6 A4 A4 A1 43 08 00 BD D0 B5 79 AD D4 A1 43 28 00 A7 F3 B4 AB AA AC BA 41 A1 41 B7 7C A8 FA AE F8 B6 C7 B0 65 A1 43 AD 6E B6 69 A6 E6 A7 F3 B4 AB AA AC BA 41 B6 DC A1 48 01 01 00 00 00 00 00 00 00 00 00 00 00 00"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk25()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("87 00 0A 00 00 00 00"));
                return mplew.getPacket();
        }
        public static MaplePacket Unk30()  {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString("93 00 07 00 00 00 00 00 00 00 01 00 00 00 00 01 00 00 CD 13 59 B0 6A 87 52 05 F6 F1 18 A0 FF FF FF FF FF FF FF FF 00 00 00 00 00 00 00 96 44 17 00 A7 BE AA D1 BE 69 C0 6E 00 00 00 00 00 00 00 00 00 84 4E 00 00 4B 75 00 00 01 00 00 0C 00 05 00 04 00 04 00 32 00 00 00 32 00 00 00 05 00 00 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 10 27 00 00 02 00 00 00 D5 86 DE 77 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0A 00 00 00 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 14 01 08 00 A7 DA A4 A3 AD 6E B4 FA 00 00 00 64 B2 15 D5 61 C8 01 00 00 00 00 96 44 17 00 00 00 00 00 00 00 00 00 18 18 18 18 60 00 40 E0 FD 3B 37 4F 01 05 00 01 8A DE 0F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 07 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF FF FF FF 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 06 00 01 A6 2C 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 07 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF FF FF FF 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 07 00 01 85 5B 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF FF FF FF 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 0B 00 01 F0 DD 13 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 07 00 00 00 00 00 00 00 00 00 00 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF FF FF FF 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 02 E9 7D 3F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 02 00 49 00 00 00 00 00 00 00 00 80 05 BB 46 E6 17 02 0C 00 00 00 00 00 00 00 00 80 05 BB 46 E6 17 02 00 00 03 00 5F 2B 01 00 30 C5 1D 00 00 D3 2A 01 00 31 00 00 00 00 00 00 00 00 00 00 FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 50 28 56 2C 71 48 CC 01 64 00 00 00 00 01"));
                
                return mplew.getPacket();
        }
}