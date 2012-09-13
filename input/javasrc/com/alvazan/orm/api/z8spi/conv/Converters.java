package com.alvazan.orm.api.z8spi.conv;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;


public class Converters {

	public static final StringConverter STRING_CONVERTER = new StringConverter();
	public static final BooleanConverter BOOLEAN_CONVERTER = new BooleanConverter();
	public static final ShortConverter SHORT_CONVERTER = new ShortConverter();
	public static final IntConverter INT_CONVERTER = new IntConverter();
	public static final LongConverter LONG_CONVERTER = new LongConverter();
	public static final FloatConverter FLOAT_CONVERTER = new FloatConverter();
	public static final DoubleConverter DOUBLE_CONVERTER = new DoubleConverter();
	public static final ByteConverter BYTE_CONVERTER = new ByteConverter();
	public static final ByteArrayConverter BYTE_ARRAY_CONVERTER = new ByteArrayConverter();
	public static final BaseConverter BIGINTEGER_CONVERTER = new BigIntegerConverter();
	public static final BaseConverter BIGDECIMAL_CONVERTER = new BigDecimalConverter();
	public static final BaseConverter LOCAL_DATE_TIME = new LocalDateTimeConverter();
	public static final BaseConverter DATE_TIME = new DateTimeConverter();
	public static final BaseConverter LOCAL_DATE = new LocalDateConverter();
	public static final BaseConverter LOCAL_TIME = new LocalTimeConverter();

	private static byte[] intToBytes(int val) {
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(outBytes);
			out.writeInt(val);
			byte[] outData = outBytes.toByteArray();
			return outData;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static class BigDecimalConverter extends BaseConverter {
		@Override
		public byte[] convertToNoSql(Object input) {
			if(input == null)
				return null;
			
			BigDecimal value = (BigDecimal) input;
			BigInteger bi = value.unscaledValue();
			Integer scale = value.scale();
			byte[] bibytes = bi.toByteArray();
			byte[] sbytes = intToBytes(scale);
			byte[] bytes = new byte[bi.toByteArray().length+4];
			
			for (int i = 0 ; i < 4 ; i++) bytes[i] = sbytes[i];
			for (int i = 4 ; i < bibytes.length+4 ; i++) bytes[i] = bibytes[i-4];

			return bytes;
		}

		@Override
		public Object convertFromNoSql(byte[] value) {
			ByteBuffer buf = ByteBuffer.wrap(value);
			int scale = buf.getInt();
			byte[] bibytes = new byte[buf.remaining()];
			buf.get(bibytes);
			 
			BigInteger bi = new BigInteger(bibytes);
			return new BigDecimal(bi,scale);
		}

		@Override
		public Object convertToType(String value) {
			return new BigDecimal(value);
		}
	}
	
	public static class BigIntegerConverter extends BaseConverter {
		@Override
		public byte[] convertToNoSql(Object value) {
			if(value == null)
				return null;
			BigInteger val = (BigInteger)value;
			return val.toByteArray();
		}

		@Override
		public Object convertFromNoSql(byte[] value) {
			return new BigInteger(value);
		}

		@Override
		public Object convertToType(String value) {
			return new BigInteger(value);
		}
		
	}
	
	public static class ByteArrayConverter extends BaseConverter {

		@Override
		public byte[] convertToNoSql(Object value) {
			return (byte[])value;
		}

		@Override
		public Object convertFromNoSql(byte[] value) {
			return value;
		}
		
		@Override
		public String convertToString(Object dbValue) {
			byte[] data = (byte[]) dbValue;
			return new String(Hex.encodeHex(data));
		}

		@Override
		public Object convertToType(String value) {
			try {
				return Hex.decodeHex(value.toCharArray());
			} catch (DecoderException e) {
				throw new RuntimeException("could not decode", e);
			}
		}
	}
	
	public static abstract class BaseConverter implements Converter {
		public Object convertStringToType(String value) {
			if(value == null)
				return null;
			
			return convertToType(value);
		}
		
		public String convertTypeToString(Object dbValue) {
			if(dbValue == null)
				return null;
			return convertToString(dbValue);
		}
		
		protected abstract Object convertToType(String value);
		
		protected String convertToString(Object value) {
			return value+"";
		}
	}
	
	public static abstract class DecimalConverter extends BaseConverter {
		public byte[] convertToNoSql(Object value) {
			if(value == null)
				return null;
			
			BigDecimal dec = convertToForSmallStorage(value);
			return BIGDECIMAL_CONVERTER.convertToNoSql(dec);
		}
		public Object convertFromNoSql(byte[] data) {
			if(data == null)
				return null;
			
			BigDecimal bigD = (BigDecimal) BIGDECIMAL_CONVERTER.convertFromNoSql(data);
			return convertFromForSmallStorage(bigD);
		}
		protected abstract Object convertFromForSmallStorage(BigDecimal bigD);
		protected abstract BigDecimal convertToForSmallStorage(Object value);
	}

	public static abstract class IntegerConverter extends BaseConverter {
		public byte[] convertToNoSql(Object value) {
			if(value == null)
				return null;
			
			BigInteger dec = convertToForSmallStorage(value);
			return BIGINTEGER_CONVERTER.convertToNoSql(dec);
		}
		public Object convertFromNoSql(byte[] data) {
			if(data == null)
				return null;
			
			BigInteger bigD = (BigInteger) BIGINTEGER_CONVERTER.convertFromNoSql(data);
			return convertFromForSmallStorage(bigD);
		}
		protected abstract Object convertFromForSmallStorage(BigInteger bigD);
		protected abstract BigInteger convertToForSmallStorage(Object value);
	}
	
	public static class StringConverter extends BaseConverter {
		
		@Override
		public byte[] convertToNoSql(Object value) {
			try {
				if(value == null)
					return null;
				String temp = (String) value;
				return temp.getBytes("UTF8");
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public Object convertFromNoSql(byte[] bytes) {
			try {
				if(bytes == null)
					return null;
				return new String(bytes, "UTF8");
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected Object convertToType(String value) {
			return value;
		}
	}
	
	public static class ByteConverter extends IntegerConverter {
		@Override
		protected Object convertToType(String value) {
			return Byte.parseByte(value);
		}
		@Override
		protected Object convertFromForSmallStorage(BigInteger bigD) {
			return bigD.byteValue();
		}

		@Override
		protected BigInteger convertToForSmallStorage(Object value) {
			Byte b = (Byte)value;
			return BigInteger.valueOf(b.byteValue());
		}
	}
	
	public static class ShortConverter extends IntegerConverter {
		@Override
		protected Object convertToType(String value) {
			return Short.parseShort(value);
		}
		@Override
		protected Object convertFromForSmallStorage(BigInteger bigD) {
			return bigD.shortValue();
		}
		@Override
		protected BigInteger convertToForSmallStorage(Object value) {
			Short s = (Short)value;
			return BigInteger.valueOf(s);
		}
	}
	
	public static class IntConverter extends IntegerConverter {
		@Override
		protected Object convertToType(String value) {
			return Integer.parseInt(value);
		}
		@Override
		protected Object convertFromForSmallStorage(BigInteger bigD) {
			return bigD.intValue();
		}
		@Override
		protected BigInteger convertToForSmallStorage(Object value) {
			Integer val = (Integer)value;
			return BigInteger.valueOf(val.intValue());
		}
	}

	public static class LongConverter extends IntegerConverter {
		@Override
		protected Object convertToType(String value) {
			return Long.parseLong(value);
		}
		@Override
		protected Object convertFromForSmallStorage(BigInteger bigD) {
			return bigD.longValue();
		}
		@Override
		protected BigInteger convertToForSmallStorage(Object value) {
			Long val = (Long)value;
			return BigInteger.valueOf(val);
		}
	}
	
	public static class FloatConverter extends DecimalConverter {
		@Override
		protected Object convertToType(String value) {
			return Float.parseFloat(value);
		}

		@Override
		protected Object convertFromForSmallStorage(BigDecimal bigD) {
			return bigD.floatValue();
		}

		@Override
		protected BigDecimal convertToForSmallStorage(Object value) {
			Float f = (Float)value;
			//NOTE: Must convert to string here or when you feed BigDecimal, the
			//2.33 that you had given the float becomes 2.329999999991234... 
			return new BigDecimal(f.floatValue()+"");
		}
	}
	
	public static class DoubleConverter extends DecimalConverter {
		@Override
		protected Object convertToType(String value) {
			return Double.parseDouble(value);
		}

		@Override
		protected Object convertFromForSmallStorage(BigDecimal bigD) {
			return bigD.doubleValue();
		}

		@Override
		protected BigDecimal convertToForSmallStorage(Object value) {
			Double d = (Double)value;
			//NOTE: Must convert to string here or when you feed BigDecimal, the
			//2.33 that you had given the float becomes 2.329999999991234...
			return new BigDecimal(""+d.doubleValue());
		}
	}
	
	public static class BooleanConverter extends BaseConverter {
		@Override
		public byte[] convertToNoSql(Object value) {
			if(value == null)
				return null;
			Boolean b = (Boolean) value;
			if(b)
				return StandardConverters.convertToBytes(1);
			return StandardConverters.convertToBytes(0);
		}
		
		@Override
		public Object convertFromNoSql(byte[] bytes) {
			if(bytes == null)
				return null;
			Integer value = StandardConverters.convertFromBytes(Integer.class, bytes);
			if(value == 1)
				return true;
			return false;
		}
		
		@Override
		protected Object convertToType(String value) {
			return Boolean.parseBoolean(value);
		}
	}
	
	public static class LocalDateTimeConverter extends BaseConverter {
		private DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		
		@Override
		public byte[] convertToNoSql(Object value) {
			if(value == null)
				return null;
			LocalDateTime dt = (LocalDateTime) value;
			long milliseconds = dt.toDate().getTime();
			return StandardConverters.convertToBytes(milliseconds);
		}

		@Override
		public Object convertFromNoSql(byte[] value) {
			if(value == null)
				return null;
			Long time = StandardConverters.convertFromBytes(Long.class, value);
			LocalDateTime dt = new LocalDateTime(time);
			return dt;
		}

		@Override
		protected Object convertToType(String value) {
			LocalDateTime dt = fmt.parseLocalDateTime(value);
			return dt;
		}

		@Override
		protected String convertToString(Object value) {
			LocalDateTime dt = (LocalDateTime) value;
			return fmt.print(dt);
		}
	}

	public static class LocalDateConverter extends BaseConverter {
		private DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		
		@Override
		public byte[] convertToNoSql(Object value) {
			if(value == null)
				return null;
			LocalDate dt = (LocalDate) value;
			long milliseconds = dt.toDate().getTime();
			return StandardConverters.convertToBytes(milliseconds);
		}

		@Override
		public Object convertFromNoSql(byte[] value) {
			if(value == null)
				return null;
			Long time = StandardConverters.convertFromBytes(Long.class, value);
			LocalDate dt = new LocalDate(time);
			return dt;
		}

		@Override
		protected Object convertToType(String value) {
			LocalDate dt = fmt.parseLocalDate(value);
			return dt;
		}

		@Override
		protected String convertToString(Object value) {
			LocalDate dt = (LocalDate) value;
			return fmt.print(dt);
		}
	}
	
	public static class LocalTimeConverter extends BaseConverter {
		private DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		
		@Override
		public byte[] convertToNoSql(Object value) {
			if(value == null)
				return null;
			LocalTime dt = (LocalTime) value;
			long milliseconds = dt.getMillisOfDay();
			return StandardConverters.convertToBytes(milliseconds);
		}

		@Override
		public Object convertFromNoSql(byte[] value) {
			if(value == null)
				return null;
			Long time = StandardConverters.convertFromBytes(Long.class, value);
			LocalTime dt = LocalTime.fromMillisOfDay(time);
			return dt;
		}

		@Override
		protected Object convertToType(String value) {
			LocalTime dt = fmt.parseLocalTime(value);
			return dt;
		}

		@Override
		protected String convertToString(Object value) {
			LocalTime dt = (LocalTime) value;
			return fmt.print(dt);
		}
	}
	
	public static class DateTimeConverter extends BaseConverter {
		private DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		
		@Override
		public byte[] convertToNoSql(Object value) {
			if(value == null)
				return null;
			DateTime dt = (DateTime) value;
			long milliseconds = dt.toDate().getTime();
			return StandardConverters.convertToBytes(milliseconds);
		}

		@Override
		public Object convertFromNoSql(byte[] value) {
			if(value == null)
				return null;
			Long time = StandardConverters.convertFromBytes(Long.class, value);
			DateTime dt = new DateTime(time);
			return dt;
		}

		@Override
		protected Object convertToType(String value) {
			DateTime dt = fmt.parseDateTime(value);
			return dt;
		}

		@Override
		protected String convertToString(Object value) {
			DateTime dt = (DateTime) value;
			return fmt.print(dt);
		}
	}
}
