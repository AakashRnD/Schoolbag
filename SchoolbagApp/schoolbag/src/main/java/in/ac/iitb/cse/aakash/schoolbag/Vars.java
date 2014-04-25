package in.ac.iitb.cse.aakash.schoolbag;

import android.os.Environment;

public
class Vars{
	public static final String SD_CARD=Environment.getExternalStorageDirectory().getPath();

	enum Path{
		Schoolbag,Class;

		@Override
		public
		String toString(){
			return name().toLowerCase();
		}
	}

}
