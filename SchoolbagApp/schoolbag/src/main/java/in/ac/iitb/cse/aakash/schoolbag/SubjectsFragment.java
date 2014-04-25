package in.ac.iitb.cse.aakash.schoolbag;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public
class SubjectsFragment extends Fragment{
	private static final String STATE_SELECTED_POSITION ="selected_navigation_drawer_position";
	private static final String PREF_USER_LEARNED_DRAWER="navigation_drawer_learned";
	private NavigationDrawerCallbacks mCallbacks;
	private ActionBarDrawerToggle     mDrawerToggle;
	private DrawerLayout              mDrawerLayout;
	private ListView                  mDrawerListView;
	private View                      mFragmentContainerView;
	private int mCurrentSelectedPosition=0;
	private boolean mFromSavedInstanceState;
	private boolean mUserLearnedDrawer;
	public ArrayList<String> subjects=new ArrayList<>();

	public
	SubjectsFragment(){ }

	@Override
	public
	void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		subjects=getSubjects(getBoard(),getMedium(),getStd());
		SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(getActivity());
		mUserLearnedDrawer=sp.getBoolean(PREF_USER_LEARNED_DRAWER,false);
		if(savedInstanceState!=null){
			mCurrentSelectedPosition=savedInstanceState.getInt(STATE_SELECTED_POSITION);
			mFromSavedInstanceState=true;
		}
		selectItem(mCurrentSelectedPosition);
	}

	private
	String getMedium(){
		SharedPreferences sharedPref=PreferenceManager.getDefaultSharedPreferences(getActivity());
		String medium=sharedPref.getString("medium_list","0");
		String[] texts=getResources().getStringArray(R.array.pref_medium_list_titles);
		String[] values=getResources().getStringArray(R.array.pref_medium_list_values);
		for(int i=0;i<values.length;i++){
			if(values[i].contentEquals(medium)) return texts[i];
		}
		return "English";
	}

	private
	String getBoard(){
		SharedPreferences sharedPref=PreferenceManager.getDefaultSharedPreferences(getActivity());
		String board=sharedPref.getString("board_list","0");
		String[] texts=getResources().getStringArray(R.array.pref_board_list_titles);
		String[] values=getResources().getStringArray(R.array.pref_board_list_values);
		for(int i=0;i<values.length;i++){
			if(values[i].contentEquals(board)) return texts[i];
		}
		return "NCERT";
	}

	private
	int getStd(){
		SharedPreferences sharedPref=PreferenceManager.getDefaultSharedPreferences(getActivity());
		return Integer.parseInt(sharedPref.getString("std_list","7"));
	}

	@Override
	public
	void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		// Indicate that this fragment would like to influence the set of actions in the action
		// bar.
		setHasOptionsMenu(true);
	}

	@Override
	public
	View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
		mDrawerListView=(ListView)inflater.inflate(R.layout.fragment_subjects_drawer,
		                                           container,
		                                           false);
		mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@Override
			public
			void onItemClick(AdapterView<?> parent,View view,int position,long id){
				selectItem(position);
			}
		});
		mDrawerListView.setAdapter(new ArrayAdapter<String>(getActivity(),
		                                              android.R.layout
		                                                .simple_list_item_activated_1,
		                                              android.R.id.text1,
		                                              subjects
		));
		mDrawerListView.setItemChecked(mCurrentSelectedPosition,true);
		return mDrawerListView;
	}

	private
	ArrayList<String> getSubjects(String board,String medium,int std){
		subjects=new ArrayList<>();
		File dir=new File(Vars.SD_CARD+"/"+Vars.Path.Schoolbag+
		                  "/"+board+"/"+medium+
		                  "/"+Vars.Path.Class+" "+std+"/");
		if(dir==null||!dir.exists()){
			Toast.makeText(getActivity(),"Not found",Toast.LENGTH_LONG).show();
			return subjects;
		}
		File[] files=dir.listFiles();
		if(files!=null) for(File f : files)
			if(f.isDirectory()) subjects.add(f.getName().toUpperCase());
		return subjects;
	}

	public
	boolean isDrawerOpen(){
		return mDrawerLayout!=null&&mDrawerLayout.isDrawerOpen(mFragmentContainerView);
	}

	public
	void setUp(int fragmentId,DrawerLayout drawerLayout){
		mFragmentContainerView=getActivity().findViewById(fragmentId);
		mDrawerLayout=drawerLayout;
		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,GravityCompat.START);
		ActionBar actionBar=getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		mDrawerToggle=new ActionBarDrawerToggle(getActivity(),
		                                        mDrawerLayout,
		                                        R.drawable.ic_drawer,
		                                        R.string.navigation_drawer_open,
		                                        R.string.navigation_drawer_close){
			@Override
			public
			void onDrawerClosed(View drawerView){
				super.onDrawerClosed(drawerView);
				if(!isAdded()){
					return;
				}
				getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
			}

			@Override
			public
			void onDrawerOpened(View drawerView){
				super.onDrawerOpened(drawerView);
				if(!isAdded()) return;
				if(!mUserLearnedDrawer){
					mUserLearnedDrawer=true;
					SharedPreferences
					  sp
					  =PreferenceManager.getDefaultSharedPreferences(getActivity());
					sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER,true).apply();
				}
				getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
			}
		};
		if(!mUserLearnedDrawer&&!mFromSavedInstanceState){
			mDrawerLayout.openDrawer(mFragmentContainerView);
		}
		// Defer code dependent on restoration of previous instance state.
		mDrawerLayout.post(new Runnable(){
			@Override
			public
			void run(){
				mDrawerToggle.syncState();
			}
		});
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private
	void selectItem(int position){
		mCurrentSelectedPosition=position;
		if(mDrawerListView!=null){
			mDrawerListView.setItemChecked(position,true);
		}
		if(mDrawerLayout!=null){
			mDrawerLayout.closeDrawer(mFragmentContainerView);
		}
		if(mCallbacks!=null){
			mCallbacks.onNavigationDrawerItemSelected(Vars.SD_CARD
			                                          +"/"
			                                          +Vars.Path.Schoolbag
			                                          +"/"
			                                          +getBoard()
			                                          +"/"
			                                          +getMedium()
			                                          +"/"
			                                          +Vars.Path.Class
			                                          +" "
			                                          +getStd()
			                                          +"/"
			                                          +subjects.get(position)
			                                          +"/");
		}
	}

	@Override
	public
	void onAttach(Activity activity){
		super.onAttach(activity);
		try{
			mCallbacks=(NavigationDrawerCallbacks)activity;
		}
		catch(ClassCastException e){
			throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
		}
	}

	@Override
	public
	void onDetach(){
		super.onDetach();
		mCallbacks=null;
	}

	@Override
	public
	void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SELECTED_POSITION,mCurrentSelectedPosition);
	}

	@Override
	public
	void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		// Forward the new configuration the drawer toggle component.
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public
	void onCreateOptionsMenu(Menu menu,MenuInflater inflater){
		// If the drawer is open, show the global app actions in the action bar. See also
		// showGlobalContextActionBar, which controls the top-left area of the action bar.
		if(mDrawerLayout!=null&&isDrawerOpen()){
			inflater.inflate(R.menu.global,menu);
			showGlobalContextActionBar();
		}
		super.onCreateOptionsMenu(menu,inflater);
	}

	@Override
	public
	boolean onOptionsItemSelected(MenuItem item){
		if(mDrawerToggle.onOptionsItemSelected(item)){
			return true;
		}
		if(item.getItemId()==R.id.action_mode){
			Toast.makeText(getActivity(),"To be implemented",Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Per the navigation drawer design guidelines, updates the action bar to show the global app
	 * 'context', rather than just what's in the current screen.
	 */
	private
	void showGlobalContextActionBar(){
		ActionBar actionBar=getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setTitle(R.string.app_name);
	}

	private
	ActionBar getActionBar(){
		return getActivity().getActionBar();
	}

	public static
	interface NavigationDrawerCallbacks{
		void onNavigationDrawerItemSelected(String subject_path);
	}
}
