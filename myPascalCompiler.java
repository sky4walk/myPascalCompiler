//written by Andre Betz 
//http://www.andrebetz.de

import java.io.*;

public class myPascalCompiler {
	private static String test_source = 		   

   		"  program syntax;                         \n"+	//1
		"  var i, j : integer;                     \n"+	//2
   		"      a : array [1..10] of char;          \n"+	//3
		"      procedure p()                       \n"+	//4
		"         var j : integer;                 \n"+	//5
		"         begin                            \n"+	//6
		"            a[i] := (a[i]+j)*(i+j) ;            \n"+	//7
		"            if ((j=i) or not (j<>i)) then \n"+	//8
		"			 begin						   \n"+	//9
		"               a[i] := ' ' + a[i] + 2;    \n"+	//10
		" 			 end						   \n"+	//11
		"            else a[i]:= 'a';              \n"+	//12
		"            if (j=i) or not (j<>i) then   \n"+	//13
		"               a[i] := ' ';               \n"+	//14
		"            else a[i]:= 'b';              \n"+	//15
		"            i := i + 1;                   \n"+	//16
		"         end;                             \n"+	//17
		"      begin                               \n"+	//18
		"         i := 1; j := 1000000;            \n"+	//19
		"         p();                             \n"+ //20
		"         while (i>0) and (i<11) or        \n"+	//21
		"               (i>=1) and (i<= 10) do     \n"+	//22
		"            i := i + 1;                   \n"+	//23
		"      end.                                \n";	//24
	
	
	private static String ToXML(Parser.ParseTree pt, int depth){
		String Text = "";
		if(pt!=null)
		{
			String Row = "";
			for(int i=0;i<depth;i++){
				Row += "\t";
			}
			while(pt!=null){
				String sym = pt.get_Symbol();
				
				if(pt.getM_Start()!=null){
					Text += Row + "<Ast sym=\""+sym+"\">\r\n";
					Text += ToXML(pt.getM_Start(),depth+1);
					Text += Row + "</Ast>\r\n";
				}else{
					Text += Row + "<Term sym=\"" + sym + "\" />\r\n";					
				}
				pt = pt.getM_Next();
			}
		}
		return Text;
	}
	private static String Compile(String source){
		String ret = "";
		if(source.length()>0){
			Scanner sc = new Scanner(source);
			int PosNr = sc.getTokens();
			if(PosNr<0){
				Parser ps = new Parser(sc);
				if(!ps.Parse()){
					Parser.ParseTree pt = ps.getAST();
					CodeGenerator cg = new CodeGenerator(pt,256);
					ret = cg.GetCode();
					if(cg.isError()){
						ret = "Context Error: " + cg.getErrorStr();
					}
//					ret = ToXML(pt,0);
				}else{
					ret = "Parse Error: " + ps.getErrorStr();
				}
			}else{
				ret = "Scanner Error Line: " + sc.GetLine();
			}
		}
		return ret;
	}
	private static String Load(String FileName){
		StringBuffer readinput = new StringBuffer();
		if(FileName==null){
			return null;
		}
		try {
			File f = new File(FileName);
			FileReader in = new FileReader(f);
			char[] buffer = new char[128];
			int len;
			while((len = in.read(buffer))!=-1) {
				readinput.append(buffer,0,len);
			}
		}
		catch(IOException e) {
			System.out.println("Dateifehler");
		}
		return readinput.toString();  	
	}
	public static void main(String[] args) {
		String r = "";
		if(args.length==0)
			r = myPascalCompiler.Compile(myPascalCompiler.test_source);
		
		else
			r = myPascalCompiler.Compile(myPascalCompiler.Load(args[0]));
		System.out.println(r);
	}

}
