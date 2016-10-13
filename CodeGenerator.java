//written by Andre Betz 2007 
//http://www.andrebetz.de

import java.util.ArrayList;

public class CodeGenerator {
	private int PROCDEC = 1;
	private int VARDEC = 2;
	private int m_StackSize = 256;
	private int m_ConstCnt = 0;
	private int m_ProcCalls = 0;
	private int m_IfCnt = 0;
	private int m_WhileCnt = 0;
	private boolean m_bError = false;
	private String m_strError = "";
	private Parser.ParseTree m_pt = null;
	private ArrayList<Declaration> m_DeclList = new ArrayList<Declaration>();
	
	private class Context{
		private ArrayList<String> m_StringLst = new ArrayList<String>();
		private int m_depth = 0;
		public Context(String Start){
			Add(Start);
		}
		public Context(Context cx){
			for(int i=0;i<cx.m_depth;i++){
				String str = (String)cx.m_StringLst.get(i);
				Add(str);
			}
		}
		public int getDepth(){
			return m_depth;
		}
		public void Add(String Name){
			m_StringLst.add(Name);
			m_depth++;
		}
		public void Delete(){
			if(m_depth>0){
				m_depth--;
				m_StringLst.remove(m_depth);
			}
		}
		public int isInContext(Context act_cx){
			int d1 = act_cx.m_depth;
			int dc = 0;
			while(dc<d1 && dc<m_depth){
				String str1 = (String)act_cx.m_StringLst.get(dc);
				String str2 = (String)m_StringLst.get(dc);
				if(!str1.equals(str2)){
					dc = -1;
					break;
				}
				dc++;
			}
			return d1-dc;
		}
		public String GetContextString(){
			String ret = "";
			for(int i=0;i<m_StringLst.size();i++){
				if(i>0){
					ret+=".";
				}
				ret += m_StringLst.get(i);
			}
			return ret;
		}
	}
	private class Declaration{
		private String m_DeclName = "";
		private Context m_context;
		private int m_StackPos = 0;
		private int m_Size = 0;
		private int m_ClassId = 0;	
		public Declaration(Context act_cx, Parser.ParseTree pt,int ClassId){
			if(pt!=null && act_cx!=null){
				m_context = new Context(act_cx);
				m_DeclName = pt.get_Symbol();
				m_ClassId = ClassId;
			}
		}
		public int GetSize(){
			return m_Size;
		}
		public void SetPos(int pos){
			m_StackPos = pos;
		}
		public int GetPos(){
			return m_StackPos;
		}
		public Context GetContext(){
			return m_context;
		}
		public boolean ContextEquals(Context cx,String sym,int Type){
			if(m_context.GetContextString().equals(cx.GetContextString())){
				if(sym.equals(m_DeclName)){
					if(Type==m_ClassId){
						return true;
					}
				}
			}
			return false;
		}
		public boolean EqualsVarTyp(String sym,int Type){
			if(sym.equals(m_DeclName)){
				if(Type==m_ClassId){
					return true;
				}
			}
			return false;
		}
	}
	private class ProcDecl extends Declaration{
		public ProcDecl(Context act_cx,Parser.ParseTree pt){
			super(act_cx,pt,PROCDEC);
		}
	}
	private class VarDecl extends Declaration{
		private String m_Type = "";
		private int m_TypeSize;
		private boolean m_Array = false;
		private int m_ArrStart = 0;
		private int m_ArrEnd = 0;
		public boolean isArray(){
			return m_Array;
		}
		public String GetType(){
			return m_Type;
		}
		
		public VarDecl(Context act_cx, Parser.ParseTree pt){
			super(act_cx,pt,VARDEC);
			if(pt!=null && act_cx!=null){
				pt = pt.getM_Start();
				if(pt!=null){
					super.m_DeclName = pt.get_Symbol();
					pt = pt.getM_Next();
					if(pt!=null){
						m_Type = pt.get_Symbol();
						if(m_Type!=null && m_Type.equalsIgnoreCase("ARRAY")){
							m_Array = true;
							pt = pt.getM_Start();
							if(pt.getM_Next()!=null)
								m_Type = pt.getM_Next().get_Symbol();
							if(pt!=null){
								if(pt.get_Symbol().equalsIgnoreCase("..")){
									pt = pt.getM_Start();
									if(pt!=null){
										String start = pt.get_Symbol();
										m_ArrStart = Integer.parseInt(start);
										pt = pt.getM_Next();
										if(pt!=null){
											String end = pt.get_Symbol();
											m_ArrEnd = Integer.parseInt(end);
										}
									}
								}
							}
						}
						if(m_Type.equalsIgnoreCase("CHAR")){
							m_TypeSize = 1;
						}
						else if(m_Type.equalsIgnoreCase("INTEGER")){
							m_TypeSize = 1;
						}
						super.m_Size = (m_ArrEnd - m_ArrStart + 1)*m_TypeSize;
					}
				}
			}				
		}
	}
	public CodeGenerator(Parser.ParseTree pt, int StackSize){
		m_pt = pt;
		m_StackSize = StackSize;
		m_bError = false;
		m_strError = "";
	}
	public boolean isError(){
		return m_bError;
	}
	public String getErrorStr(){
		return m_strError;
	}
	public String GetCode(){
		String CodeText = "";
		if(m_pt!=null){
			
			String Start = m_pt.get_Symbol();
			Context cx = new Context(Start);
			CreateContext(cx,m_pt.getM_Start(),0);
			
			CodeText +=	"#include \"SubRoutine.asm\"\n";
			CodeText +=	"MAKRO : PROGRAM()\n";
			CodeText +=	"JMP(" + Start + ")\n";
			CodeText +=	"Stack["+m_StackSize+"] : 0\n";
			CodeText +=	"StackPos : 0\n";
			CodeText +=	"StackPosExpr : 0\n";
			CodeText +=	"Reg_X : 0\n";		
			CodeText +=	"Reg_Y : 0\n";		
			CodeText +=	"Reg_Z : 0\n";	
			CodeText += CreateCode(cx,m_pt.getM_Start(),0);
			CodeText +=	"END\n";
		}
		return CodeText;
	}
	private Declaration FindvariableExact(Context cx,String Sym,int Type){
		for(int i=0;i<m_DeclList.size();i++){
			Declaration dec = (Declaration)m_DeclList.get(i);
			if(dec.ContextEquals(cx, Sym, Type)){
				return dec;
			}
		}
		return null;
	}
	private Declaration FindvariableContext(Context cx,String Sym,int Type){
		Declaration ret = null;
		int minDist = cx.getDepth();
		for(int i=0;i<m_DeclList.size();i++){
			Declaration dec = (Declaration)m_DeclList.get(i);
			if(dec!=null){
				if(dec.EqualsVarTyp(Sym,Type)){
					int dist = cx.isInContext(dec.GetContext());
					if(dist>=0 && minDist>=dist){
						minDist = dist;
						ret = dec;
					}
				}
			}
		}
		return ret;
	}
	private int GetContextVarSize(Context cx){
		int VarSize = 0;
		for(int i=0;i<m_DeclList.size();i++){
			Declaration dec = (Declaration)m_DeclList.get(i);
			String strCx = dec.GetContext().GetContextString();
			if(strCx.equals(cx.GetContextString())){
				VarSize += dec.GetSize();
			}
		}
		return VarSize;
	}
	private String CreateConstantList(Context cx, Parser.ParseTree pt){
		String CodeText = "";
		if(pt!=null){
			while(pt!=null){
				String sym = pt.get_Symbol();
				
				if(pt.getM_Start()!=null){
					if(sym.equalsIgnoreCase("CONSTANTINT")){
						Parser.ParseTree tmp = pt.getM_Start();
						if(tmp!=null){
							CodeText += cx.GetContextString()+"_CONST_"+m_ConstCnt+" : "+tmp.get_Symbol()+"\n";
							m_ConstCnt++;
						}
					}else if(sym.equalsIgnoreCase("CONSTANTLIT")){
						Parser.ParseTree tmp = pt.getM_Start();
						if(tmp!=null){
							int sgn = 0;
							String ascii = tmp.get_Symbol();
							if(ascii.length()>0){
								sgn = ascii.charAt(0);
							}
							CodeText += cx.GetContextString()+"_CONST_"+m_ConstCnt+" : "+sgn+"\n";
							m_ConstCnt++;
						}
					}else{
						CodeText += CreateConstantList(cx,pt.getM_Start());
					}
				}else{
				}
				pt = pt.getM_Next();
			}
		}
		return CodeText;
	}
	private void CreateContext(Context cx, Parser.ParseTree pt, int depth){
		int StackPos = 0;
		while(pt!=null){
			String sym = pt.get_Symbol();
			if(sym.equalsIgnoreCase("PROCEDURE")){
				Parser.ParseTree tmp = pt.getM_Start();
				if(tmp!=null){
					sym = tmp.get_Symbol();
					ProcDecl proc = new ProcDecl(cx,tmp);
					if(FindvariableExact(cx, sym, PROCDEC)==null){
						m_DeclList.add(proc);
					}
					if(tmp.getM_Next()!=null){
						cx.Add(sym);
						tmp = tmp.getM_Next();
						CreateContext(cx,tmp.getM_Start(),depth+1);
						cx.Delete();
					}
				}
			}else if(sym.equalsIgnoreCase("VAR")){
				VarDecl var = new VarDecl(cx,pt);
				if(FindvariableExact(cx, sym, VARDEC)==null){
					var.SetPos(StackPos);
					StackPos += var.GetSize();
					m_DeclList.add(var);
				}
			}else if(sym.equalsIgnoreCase("{")){
			}
			pt = pt.getM_Next();
		}
	}
	private String CreateCode(Context cx, Parser.ParseTree pt, int StackBase){
		String CodeText = "";
		int VarSize = GetContextVarSize(cx);
		while(pt!=null){
			String sym = pt.get_Symbol();		
			if(sym.equalsIgnoreCase("PROCEDURE")){
				Parser.ParseTree tmp = pt.getM_Start();
				if(tmp!=null){
					sym = tmp.get_Symbol();
					Declaration dec = FindvariableExact(cx,sym,PROCDEC);
					if(dec!=null){
						if(tmp.getM_Next()!=null){
							cx.Add(sym);
							tmp = tmp.getM_Next();
							CodeText += "#PROCEDURE : "+sym+" - Begin\n";
							CodeText += CreateCode(cx,tmp.getM_Start(),StackBase+VarSize);
							CodeText += "RTS(Stack,StackPos)\n";
							CodeText += "#PROCEDURE : "+sym+" - End\n";
							cx.Delete();
						}
					}
				}
			}
			else if(sym.equalsIgnoreCase("VAR")){
				Parser.ParseTree tmp = pt.getM_Start();
				if(tmp!=null){
					sym = tmp.get_Symbol();
					Declaration dec = FindvariableExact(cx,sym,VARDEC);
					if(dec!=null){
						CodeText +=	cx.GetContextString()+"_StkPos_"+sym+" : "+dec.GetPos()+"\n";
					}
				}
			}
			else if(sym.equalsIgnoreCase("{")){
				m_ConstCnt = 0;
				m_ProcCalls = 0;
				m_IfCnt = 0;
				m_WhileCnt = 0;
				CodeText += cx.GetContextString()+"_VarSz : " + VarSize + "\n";
				CodeText += cx.GetContextString()+"_StkBase : " + StackBase + "\n";
				CodeText +=	CreateConstantList(cx,pt);
				CodeText +=	cx.GetContextString()+" : ";
				CodeText += "\n";
				CodeText += "CPY(StackPos,"+cx.GetContextString()+"_StkBase)\n";
				CodeText += "ADD(StackPos,"+cx.GetContextString()+"_VarSz,StackPosExpr)\n";
				m_ConstCnt = 0;
				CodeText += Statements(cx,pt.getM_Start(),VarSize);
			}
			pt = pt.getM_Next();
		}
		return CodeText;
	}	
	private String Statements(Context cx, Parser.ParseTree pt,int VarSize){
		String CodeText = "";
		String ConStr = cx.GetContextString();
		int actIf = 0;
		int actWhile = 0;
		while(pt!=null){
			String sym = pt.get_Symbol();
			if(sym.equalsIgnoreCase(":=")){
				Parser.ParseTree storeVar = pt.getM_Start();
				if(storeVar!=null){
					Parser.ParseTree expr = storeVar.getM_Next();
					CodeText += Expression(cx, expr);
					sym = storeVar.get_Symbol();
					if(sym.equals("[")){
						CodeText += "POP(Stack,StackPosExpr,Reg_Z)\n";
						storeVar = storeVar.getM_Start();
						if(storeVar!=null){
							sym = storeVar.get_Symbol();
							Declaration dec = FindvariableContext(cx,sym, VARDEC);
							if(dec!=null){
								expr = storeVar.getM_Next();
								if(expr!=null){
									CodeText += Expression(cx,expr);
									CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
									String contextVar = dec.GetContext().GetContextString();
									CodeText += "ADD("+contextVar+"_StkPos_"+sym+",Reg_X,Reg_Y)\n";
									CodeText += "ST(Stack,Reg_Y,Reg_Z)\n";
								}
							}else{
								m_bError = true;
								m_strError += "Error: Cannot find "+sym+" in context "+ConStr+" !\n";
							}
						}
					}else{
						Declaration dec = FindvariableContext(cx,sym, VARDEC);
						if(dec!=null){
							String contextVar = dec.GetContext().GetContextString();
							CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
							CodeText += "ADD("+contextVar+"_StkBase,"+contextVar+"_StkPos_"+sym+",Reg_Y)\n";
							CodeText += "ST(Stack,Reg_Y,Reg_X)\n";
						}else{
							m_bError = true;
							m_strError += "Error: Cannot find "+sym+" in context "+ConStr+" !\n";
						}
					}
				}
			}else if(sym.equalsIgnoreCase("WHILE")){
				Parser.ParseTree expr = pt.getM_Start();
				if(expr!=null){
					actWhile = m_WhileCnt;
					m_WhileCnt++;
					CodeText += cx.GetContextString()+"_WHILE_Start_"+actWhile+" : ";
					CodeText += "\n";
					CodeText += Expression(cx, expr);
					CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
					CodeText += "BZ(Reg_X,"+cx.GetContextString()+"_WHILE_End_"+actWhile+")\n";					
					Parser.ParseTree statmnts =  expr.getM_Next();
					if(statmnts!=null){
						if(statmnts.get_Symbol().equalsIgnoreCase("{")){
							statmnts =  statmnts.getM_Start();
						}
						while(statmnts!=null){
							CodeText += Statements(cx, statmnts, VarSize);
							statmnts =  statmnts.getM_Next();
						}
					}
					CodeText += "JMP("+cx.GetContextString()+"_WHILE_Start_"+actWhile+")\n";
					CodeText += cx.GetContextString()+"_WHILE_End_"+actWhile+" : ";
					CodeText += "\n";
				}
			}else if(sym.equalsIgnoreCase("IF")){
				Parser.ParseTree expr = pt.getM_Start();
				if(expr!=null){
					actIf = m_IfCnt;
					m_IfCnt++;
					CodeText += cx.GetContextString()+"_IF_Start_"+actIf+" : ";
					CodeText += "\n";
					CodeText += Expression(cx, expr);
					CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
					CodeText += "BZ(Reg_X,"+cx.GetContextString()+"_IF_Else_"+actIf+")\n";
					Parser.ParseTree statmnts =  expr.getM_Next();
					if(statmnts!=null){
						if(statmnts.get_Symbol().equalsIgnoreCase("{")){
							Parser.ParseTree substatmnts =  statmnts.getM_Start();
							while(substatmnts!=null){
								CodeText += Statements(cx, substatmnts, VarSize);
								substatmnts =  substatmnts.getM_Next();
							}
						}else{
							CodeText += Statements(cx, statmnts, VarSize);
						}
						CodeText += cx.GetContextString()+"_IF_Else_"+actIf+" : ";
						CodeText += "\n";
						statmnts =  expr.getM_Next();
						if(statmnts!=null){
							if(statmnts.get_Symbol().equalsIgnoreCase("{")){
								Parser.ParseTree substatmnts =  statmnts.getM_Start();
								while(substatmnts!=null){
									CodeText += Statements(cx, substatmnts, VarSize);
									substatmnts =  substatmnts.getM_Next();
								}
							}else{
								CodeText += Statements(cx, statmnts, VarSize);
							}
						}
						CodeText += "JMP("+cx.GetContextString()+"_IF_End_"+actIf+")\n";
					}
					CodeText += cx.GetContextString()+"_IF_End_"+actIf+" : ";
					CodeText += "\n";
				}
			}else if(sym.equalsIgnoreCase("PROC_CALL")){
				Parser.ParseTree tmp = pt.getM_Start();
				sym = tmp.get_Symbol();
				Declaration dec = FindvariableContext(cx,sym, PROCDEC);
				if(dec!=null){
					String CallConStr = dec.GetContext().GetContextString();
					CodeText += "#PROC_CALL : " + sym + " - Begin\n";
					CodeText += "CPY(StackPos,"+cx.GetContextString()+"_StkBase)\n";
					CodeText +=	"ADD(StackPos,"+ConStr+"_VarSz,StackPos)\n";
					CodeText +=	"ADD(StackPos,"+CallConStr+"."+sym+"_VarSz,StackPosExpr)\n";
					CodeText +=	"JSR(Stack,StackPos,"+ConStr+"."+sym+"_Ret_"+m_ProcCalls+","+CallConStr+"."+sym+")\n";
					CodeText +=	ConStr+"."+sym+"_Ret_"+m_ProcCalls+" : ";
					CodeText += "\n";
					CodeText +=	"SUB(StackPos,"+ConStr+"_VarSz,StackPos)\n";
					CodeText += "ADD(StackPos,"+cx.GetContextString()+"_VarSz,StackPosExpr)\n";
					CodeText += "CPY(StackPos,"+cx.GetContextString()+"_StkBase)\n";
					CodeText += "#PROC_CALL : " + sym + " - End\n";
					m_ProcCalls++;
				}else{
					m_bError = true;
					m_strError += "Error: Cannot find "+sym+" in context "+ConStr+" !\n";
				}
			}
			pt = pt.getM_Next();
		}
		return CodeText;
	}
	private String Expression(Context cx, Parser.ParseTree pt){
		String CodeText = "";
		String ConStr = cx.GetContextString();
		if(pt!=null){
			String sym = pt.get_Symbol();
			if(pt.getM_Start()!=null){
				Parser.ParseTree tmp = pt.getM_Start();
				if(sym.equalsIgnoreCase("CONSTANTINT")||sym.equalsIgnoreCase("CONSTANTLIT")){
					if(tmp!=null){
						CodeText += "PSH(Stack,StackPosExpr,"+cx.GetContextString()+"_CONST_"+m_ConstCnt+")\n";
						m_ConstCnt++;
					}
				}else if(sym.equalsIgnoreCase("[")){
					if(tmp!=null){
						sym = tmp.get_Symbol();
						Declaration dec = FindvariableContext(cx,sym, VARDEC);
						if(dec!=null){
							Parser.ParseTree expr = tmp.getM_Next();
							if(expr!=null){
								CodeText += Expression(cx,expr);
								CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
								String contextVar = dec.GetContext().GetContextString();
								CodeText += "ADD("+contextVar+"_StkPos_"+sym+",Reg_X,Reg_X)\n";
								CodeText += "LD(Stack,Reg_X,Reg_Y)\n";
								CodeText += "PSH(Stack,StackPosExpr,Reg_Y)\n";
							}
						}else{
							m_bError = true;
							m_strError += "Error: Cannot find "+sym+" in context "+ConStr+" !\n";
						}
					}
				}else{
					while(tmp!=null){
						CodeText += Expression(cx, tmp);
						tmp = tmp.getM_Next();
					}
					if      (sym.equalsIgnoreCase("=")){
						CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
						CodeText += "POP(Stack,StackPosExpr,Reg_Y)\n";
						CodeText += "EQU(Reg_X,Reg_Y,Reg_Y)\n";
						CodeText += "PSH(Stack,StackPosExpr,Reg_Y)\n";
					}else if(sym.equalsIgnoreCase("<>")){
						CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
						CodeText += "POP(Stack,StackPosExpr,Reg_Y)\n";
						CodeText += "NEQ(Reg_X,Reg_Y,Reg_Y)\n";
						CodeText += "PSH(Stack,StackPosExpr,Reg_Y)\n";
					}else if(sym.equalsIgnoreCase("<")){
						CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
						CodeText += "POP(Stack,StackPosExpr,Reg_Y)\n";
						CodeText += "GTE(Reg_X,Reg_Y,Reg_X)\n";
						CodeText += "NOT(Reg_X,Reg_Y)\n";
						CodeText += "PSH(Stack,StackPosExpr,Reg_Y)\n";
					}else if(sym.equalsIgnoreCase("<=")){
						CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
						CodeText += "POP(Stack,StackPosExpr,Reg_Y)\n";
						CodeText += "GT(Reg_X,Reg_Y,Reg_X)\n";
						CodeText += "NOT(Reg_X,Reg_Y)\n";
						CodeText += "PSH(Stack,StackPosExpr,Reg_Y)\n";
					}else if(sym.equalsIgnoreCase(">")){
						CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
						CodeText += "POP(Stack,StackPosExpr,Reg_Y)\n";
						CodeText += "GT(Reg_X,Reg_Y,Reg_Y)\n";
						CodeText += "PSH(Stack,StackPosExpr,Reg_Y)\n";
					}else if(sym.equalsIgnoreCase(">=")){
						CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
						CodeText += "POP(Stack,StackPosExpr,Reg_Y)\n";
						CodeText += "GTE(Reg_X,Reg_Y,Reg_Y)\n";
						CodeText += "PSH(Stack,StackPosExpr,Reg_Y)\n";
					}else if(sym.equalsIgnoreCase("+")){
						CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
						CodeText += "POP(Stack,StackPosExpr,Reg_Y)\n";
						CodeText += "ADD(Reg_X,Reg_Y,Reg_Y)\n";
						CodeText += "PSH(Stack,StackPosExpr,Reg_Y)\n";
					}else if(sym.equalsIgnoreCase("-")){
						CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
						CodeText += "POP(Stack,StackPosExpr,Reg_Y)\n";
						CodeText += "SUB(Reg_X,Reg_Y,Reg_Y)\n";
						CodeText += "PSH(Stack,StackPosExpr,Reg_Y)\n";
					}else if(sym.equalsIgnoreCase("*")){
						CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
						CodeText += "POP(Stack,StackPosExpr,Reg_Y)\n";
						CodeText += "MUL(Reg_X,Reg_Y,Reg_Y)\n";
						CodeText += "PSH(Stack,StackPosExpr,Reg_Y)\n";
					}else if(sym.equalsIgnoreCase("DIV")){
						CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
						CodeText += "POP(Stack,StackPosExpr,Reg_Y)\n";
						CodeText += "DIV(Reg_X,Reg_Y,Reg_Y)\n";
						CodeText += "PSH(Stack,StackPosExpr,Reg_Y)\n";
					}else if(sym.equalsIgnoreCase("OR")){
						CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
						CodeText += "POP(Stack,StackPosExpr,Reg_Y)\n";
						CodeText += "OR(Reg_X,Reg_Y,Reg_Y)\n";
						CodeText += "PSH(Stack,StackPosExpr,Reg_Y)\n";
					}else if(sym.equalsIgnoreCase("AND")){
						CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
						CodeText += "POP(Stack,StackPosExpr,Reg_Y)\n";
						CodeText += "AND(Reg_X,Reg_Y,Reg_Y)\n";
						CodeText += "PSH(Stack,StackPosExpr,Reg_Y)\n";
					}else if(sym.equalsIgnoreCase("NOT")){
						CodeText += "POP(Stack,StackPosExpr,Reg_X)\n";
						CodeText += "NOT(Reg_X,Reg_Y)\n";
						CodeText += "PSH(Stack,StackPosExpr,Reg_Y)\n";
					}
				}
			}else{
				Declaration dec = FindvariableContext(cx,sym, VARDEC);
				if(dec!=null){
					String contextVar = dec.GetContext().GetContextString();
					CodeText += "ADD("+contextVar+"_StkBase,"+contextVar+"_StkPos_"+sym+",Reg_X)\n";
					CodeText += "LD(Stack,Reg_X,Reg_Y)\n";
					CodeText += "PSH(Stack,StackPosExpr,Reg_Y)\n";
				}else{
					m_bError = true;
					m_strError += "Error: Cannot find "+sym+" in context "+ConStr+" !\n";
				}
			}
		}
		return CodeText;
	}
}