package com.example.domain

import com.example.data.model.CaseStudyEntry
import com.example.data.model.HelplineEntry
import com.example.data.model.HelplineRegion
import com.example.data.model.LearningModule
import com.example.data.model.LegalRightEntry
import com.example.data.model.OrganizationEntry
import com.example.data.model.QuizQuestion
import com.example.data.model.SafeZoneEntry

object SafeHerCatalog {

    val HELPLINES: List<HelplineEntry> = listOf(
        // ==================== NATIONAL EMERGENCY ====================
        HelplineEntry("h_112", "National Emergency (ERSS)", "112", "Emergency", HelplineRegion.NATIONAL, "Unified police, fire, ambulance. Works in all 36 states/UTs.", true),
        HelplineEntry("h_100", "Police Control Room", "100", "Emergency", HelplineRegion.NATIONAL, "Nationwide police control room."),
        HelplineEntry("h_101", "Fire & Rescue", "101", "Emergency", HelplineRegion.NATIONAL, "Nationwide fire services."),
        HelplineEntry("h_108", "Ambulance (Emergency)", "108", "Emergency", HelplineRegion.NATIONAL, "Accidents, trauma, critical care."),
        HelplineEntry("h_102", "Ambulance (Pregnant Women / Infants)", "102", "Emergency", HelplineRegion.NATIONAL, "JSSK – Janani Sishu Suraksha Kariyakaram."),
        HelplineEntry("h_1070", "Disaster Control (State)", "1070", "Emergency", HelplineRegion.NATIONAL, "State-level disaster control room."),
        HelplineEntry("h_1077", "Disaster Control (District)", "1077", "Emergency", HelplineRegion.NATIONAL, "District-level disaster control room."),
        HelplineEntry("h_1033", "National Highway Helpline", "1033", "Emergency", HelplineRegion.NATIONAL, "Accidents, breakdowns on national highways."),
        HelplineEntry("h_139", "Railway Helpline (Rail Madad)", "139", "Emergency", HelplineRegion.NATIONAL, "Security and medical help on trains."),
        HelplineEntry("h_1906", "LPG Gas Leak Emergency", "1906", "Emergency", HelplineRegion.NATIONAL, "24 hours, every oil company."),
        HelplineEntry("h_181_nat", "Women Helpline (National)", "181", "Women", HelplineRegion.NATIONAL, "Toll-free 24/7. Linked to ERSS and One Stop Centres across 35 states/UTs.", true),
        HelplineEntry("h_14490", "NCW Women Helpline", "14490", "Women", HelplineRegion.NATIONAL, "24x7 National Commission for Women short-code helpline. Linked to 7827170170.", true),
        HelplineEntry("h_ncw_alt", "NCW Helpline (Alternate)", "7827170170", "Women", HelplineRegion.NATIONAL, "NCW dedicated round-the-clock helpline for women in distress. Psychology counsellor available."),
        HelplineEntry("h_1930", "National Cyber Crime Helpline", "1930", "Cyber & Fraud", HelplineRegion.NATIONAL, "Toll-free 24/7. Golden Hour freeze for OTP/UPI scams & online harassment.", true),
        HelplineEntry("h_14416", "Tele-MANAS Mental Health", "14416", "Mental Health", HelplineRegion.NATIONAL, "National tele-mental health support, 24/7. Multilingual counsellors.", true),
        HelplineEntry("h_1098", "Childline", "1098", "Children", HelplineRegion.NATIONAL, "24-hour free nationwide emergency helpline for children.", true),
        HelplineEntry("h_15100", "NALSA Legal Aid Helpline", "15100", "Legal", HelplineRegion.NATIONAL, "Toll-free national legal aid helpline — speak with a panel lawyer.", true),
        HelplineEntry("h_14454", "Tele-Law Helpline", "14454", "Legal", HelplineRegion.NATIONAL, "Free legal advice via Common Service Centres."),
        HelplineEntry("h_14567", "Elderline (National)", "14567", "Senior Citizens", HelplineRegion.NATIONAL, "Toll-free elder helpline — rescue, legal guidance, counselling (8 AM–8 PM)."),
        HelplineEntry("h_1933", "MANAS Helpline (Drugs)", "1933", "Anti-Trafficking", HelplineRegion.NATIONAL, "Narcotics Control Bureau — report drug trafficking confidentially."),
        HelplineEntry("h_155222", "Anti-Ragging Helpline", "155222", "Children", HelplineRegion.NATIONAL, "Report ragging in educational institutions."),
        HelplineEntry("h_1097", "Govt AIDS Helpline", "1097", "Health", HelplineRegion.NATIONAL, "National AIDS control helpline."),
        HelplineEntry("h_kiran", "KIRAN Mental Health Helpline", "1800-599-0019", "Mental Health", HelplineRegion.NATIONAL, "Mental health rehabilitation and support."),
        HelplineEntry("h_vandrevala", "Vandrevala Foundation", "9999666555", "Mental Health", HelplineRegion.NATIONAL, "24/7 crisis intervention helpline."),
        HelplineEntry("h_aasra", "AASRA Crisis Line", "022-27546669", "Mental Health", HelplineRegion.OTHER, "24/7 crisis intervention and suicide prevention (Navi Mumbai / National)."),
        HelplineEntry("h_icall", "iCALL (TISS)", "022-25521111", "Mental Health", HelplineRegion.OTHER, "Psycho-social counselling and support (Mumbai / National)."),
        HelplineEntry("h_nimhans", "NIMHANS Helpline", "080-46110007", "Mental Health", HelplineRegion.OTHER, "Medical advice, counselling, and rehabilitation (Bengaluru / National)."),
        HelplineEntry("h_nhrc", "National Human Rights Commission", "011-23385368", "Legal", HelplineRegion.NATIONAL, "NHRC helpline. Mobile: 9810298900."),
        HelplineEntry("h_lcwri", "Lawyers Collective (LC WRI)", "011-24373993", "Legal", HelplineRegion.OTHER, "Pro-bono legal aid cell for domestic violence cases. Also: 011-24372923."),
        HelplineEntry("h_hrln", "Human Rights Law Network", "011-24316922", "Legal", HelplineRegion.OTHER, "Legal services and helpline. Also: 011-24324503."),
        HelplineEntry("h_marg", "MARG Legal Aid", "011-26497483", "Legal", HelplineRegion.OTHER, "Legal aid and research for women. Also: 011-26496925."),
        HelplineEntry("h_cehat", "CEHAT Helpline", "9029073154", "Women", HelplineRegion.OTHER, "24x7 helpline for women and girls facing violence (Mumbai)."),

        // ==================== PUDUCHERRY ====================
        HelplineEntry("h_py_wc", "Puducherry Women's Commission", "0413-2205153", "Women", HelplineRegion.PUDUCHERRY, "Puducherry Women's Commission. Alternate: 0413-2201928."),
        HelplineEntry("h_py_awps", "All Women PS – Puducherry", "0413-2336178", "Women Police", HelplineRegion.PUDUCHERRY, "Puducherry All Women Police Station. Email: awpspdy.pon@nic.in.", true),
        HelplineEntry("h_py_villianur", "All Women PS – Villianur", "0413-2660385", "Women Police", HelplineRegion.PUDUCHERRY, "Villianur, Puducherry. Mobile: 94421-07748."),
        HelplineEntry("h_py_cyber", "Puducherry Cyber Crime Cell", "155260", "Cyber & Fraud", HelplineRegion.PUDUCHERRY, "Puducherry cyber crime cell (09:00–18:00). Mobile: 9489205248."),
        HelplineEntry("h_py_tycl", "TYCL Mental Health (Puducherry)", "9655507090", "Mental Health", HelplineRegion.PUDUCHERRY, "Puducherry youth & community mental health support line."),
        HelplineEntry("h_py_sahodaran", "Sahodaran (Puducherry)", "9894455200", "LGBTQ+", HelplineRegion.PUDUCHERRY, "Puducherry LGBTQ+ community-based support organization."),

        // ==================== TAMIL NADU CORE & SPECIAL UNITS ====================
        HelplineEntry("h_tn_1091", "Singapen Special Force / Women Police", "1091", "Women Police", HelplineRegion.TAMIL_NADU, "Dedicated helpline for TN Police Singapen Special Force (SSF) for women & children.", true),
        HelplineEntry("h_tn_1094", "Missing Women & Children (TN)", "1094", "Children", HelplineRegion.TAMIL_NADU, "Dedicated Tamil Nadu helpline for missing women and children.", true),
        HelplineEntry("h_tn_181", "TN Women Helpline (Amma Call Center)", "181", "Women", HelplineRegion.TAMIL_NADU, "Tamil Nadu 24/7 confidential helpline at Ambattur, Chennai."),
        HelplineEntry("h_tn_w_emerg", "TN Women's Emergency (Chennai)", "044-23452365", "Women", HelplineRegion.TAMIL_NADU, "Tamil Nadu police women's emergency contact. Alternate: 9444404090."),
        HelplineEntry("h_tn_wc", "Tamil Nadu Women's Commission", "044-28592750", "Women", HelplineRegion.TAMIL_NADU, "Kalasha Mahal, Chepauk, Chennai – 600005."),
        HelplineEntry("h_tn_gas_chn", "Gas Helpline (Chennai)", "1716", "Emergency", HelplineRegion.TAMIL_NADU, "Chennai gas emergency helpline."),
        HelplineEntry("h_tn_104", "TN Health & Counselling Helpline", "104", "Health", HelplineRegion.TAMIL_NADU, "24/7 free medical consultation & student mental health counselling."),
        HelplineEntry("h_tn_14417", "TN School Education Helpline", "14417", "Children", HelplineRegion.TAMIL_NADU, "Student safety, mental health, and queries."),
        HelplineEntry("h_tn_tnslsa_tf", "TNSLSA Legal Aid Toll-Free", "1800-4252-441", "Legal", HelplineRegion.TAMIL_NADU, "Tamil Nadu State Legal Services Authority (10 AM–6 PM, working days)."),
        HelplineEntry("h_tn_tnslsa_dir", "TNSLSA Direct Office", "044-25342441", "Legal", HelplineRegion.TAMIL_NADU, "TNSLSA High Court Campus, Chennai. Mobile: 9445033363."),
        HelplineEntry("h_tn_dv_state", "DV Protection Helpline (TN State)", "8610999482", "Legal", HelplineRegion.TAMIL_NADU, "Tamil Nadu district family violence protection helpline. Alternate: 9500190590."),
        HelplineEntry("h_tn_drug", "TN Drug Trafficking Helpline", "10581", "Anti-Trafficking", HelplineRegion.TAMIL_NADU, "Report illegal narcotics sale/trafficking. WhatsApp: 9498410581."),
        HelplineEntry("h_tn_tg", "TN Transgender Welfare Board", "044-24351885", "LGBTQ+", HelplineRegion.TAMIL_NADU, "Saidapet, Chennai-15. 24/7 helpline under TN Transgender Policy 2025."),
        HelplineEntry("h_tn_disabled", "State Commissionerate for Differently Abled", "18004250111", "Disability Support", HelplineRegion.TAMIL_NADU, "TN helpline. Landline: 044-28444948."),
        HelplineEntry("h_tn_niepmd", "NIEPMD Toll-Free", "1800-425-0345", "Disability Support", HelplineRegion.TAMIL_NADU, "Persons with multiple disabilities. Tel: 044-27472113."),
        HelplineEntry("h_tn_dawo", "Differently Abled Welfare Office (Chennai)", "044-24714758", "Disability Support", HelplineRegion.TAMIL_NADU, "DMS Campus, Teynampet, Chennai."),

        // ==================== WOMEN POLICE STATIONS (TN) ====================
        HelplineEntry("h_awps_adyar", "All Women PS – Adyar", "044-23452586", "Women Police", HelplineRegion.TAMIL_NADU, "Adyar, Chennai."),
        HelplineEntry("h_awps_anna", "All Women PS – Anna Nagar", "044-23452726", "Women Police", HelplineRegion.TAMIL_NADU, "Anna Nagar, Chennai."),
        HelplineEntry("h_awps_ashok", "All Women PS – Ashok Nagar", "044-24493663", "Women Police", HelplineRegion.TAMIL_NADU, "Ashok Nagar, Chennai."),
        HelplineEntry("h_awps_ennore", "All Women PS – Ennore", "044-25750237", "Women Police", HelplineRegion.TAMIL_NADU, "Ennore, Chennai."),
        HelplineEntry("h_awps_poonam", "All Women PS – Poonamallee", "044-26492211", "Women Police", HelplineRegion.TAMIL_NADU, "Poonamallee, Chennai."),
        HelplineEntry("h_awps_puli", "All Women PS – Pulianthope (W16)", "044-26673355", "Women Police", HelplineRegion.TAMIL_NADU, "Pulianthope, Chennai."),
        HelplineEntry("h_awps_wpet", "All Women PS – Washermanpet (W13)", "044-25982488", "Women Police", HelplineRegion.TAMIL_NADU, "Washermanpet, Chennai."),
        HelplineEntry("h_awps_tambaram", "All Women PS – Tambaram", "044-22264726", "Women Police", HelplineRegion.TAMIL_NADU, "Tambaram, Chennai."),
        HelplineEntry("h_awps_cbe", "All Women PS – Coimbatore", "0422-2300818", "Women Police", HelplineRegion.TAMIL_NADU, "Coimbatore City. Also: 2305126 / 2303778."),
        HelplineEntry("h_awps_mdu", "All Women PS – Madurai", "0452-2343602", "Women Police", HelplineRegion.TAMIL_NADU, "Crime Prevention Cell, Madurai City."),
        HelplineEntry("h_awps_slm", "All Women PS – Salem", "9087200100", "Women Police", HelplineRegion.TAMIL_NADU, "Collector Office Campus, Salem."),
        HelplineEntry("h_awps_tvl", "All Women PS – Tirunelveli", "0462-2562651", "Women Police", HelplineRegion.TAMIL_NADU, "Commissioner of Police, Tirunelveli."),
        HelplineEntry("h_awps_tpr", "All Women PS – Tiruppur", "0421-2243324", "Women Police", HelplineRegion.TAMIL_NADU, "District Welfare Office, Tiruppur."),
        HelplineEntry("h_awps_try", "All Women PS – Trichy", "9626273399", "Women Police", HelplineRegion.TAMIL_NADU, "Cantonment All Women Police Station, Trichy."),
        HelplineEntry("h_awps_cud", "All Women PS – Cuddalore", "04142-284339", "Women Police", HelplineRegion.TAMIL_NADU, "District Crime Records Bureau, Cuddalore."),
        HelplineEntry("h_awps_denk", "All Women PS – Denkanikottai", "04347-235847", "Women Police", HelplineRegion.TAMIL_NADU, "Krishnagiri District."),
        HelplineEntry("h_awps_kgi", "All Women PS – Krishnagiri", "04347-235045", "Women Police", HelplineRegion.TAMIL_NADU, "Krishnagiri District."),
        HelplineEntry("h_awps_bargur", "All Women PS – Bargur", "04343-265520", "Women Police", HelplineRegion.TAMIL_NADU, "Krishnagiri District."),
        HelplineEntry("h_awps_hosur", "All Women PS – Hosur", "04344-261100", "Women Police", HelplineRegion.TAMIL_NADU, "Krishnagiri District."),
        HelplineEntry("h_awps_dpm", "Women Police Station – Dharmapuri", "04342-260058", "Women Police", HelplineRegion.TAMIL_NADU, "No. 52/N, Dharmapuri."),
        HelplineEntry("h_awps_mel", "All Women PS – Melmaruvathur", "9498110021", "Women Police", HelplineRegion.TAMIL_NADU, "Cheyyur, Chengalpattu District. Also: 9498100267."),
        HelplineEntry("h_awps_col", "All Women PS – Colachel", "04651-228500", "Women Police", HelplineRegion.TAMIL_NADU, "Kanniyakumari District."),
        HelplineEntry("h_awps_erode", "All Women PS – Erode", "9498101254", "Women Police", HelplineRegion.TAMIL_NADU, "Erode District All Women Police Station."),
        HelplineEntry("h_awps_pon", "All Women PS – Ponnaiahpuram", "04564-225778", "Women Police", HelplineRegion.TAMIL_NADU, "Thoothukudi District."),

        // ==================== PCVC, NGOs, MENTAL HEALTH, SENIORS, LGBTQ+ (TN) ====================
        HelplineEntry("h_pcvc_tf", "PCVC 24/7 Crisis Helpline", "1800-102-7282", "Women", HelplineRegion.TAMIL_NADU, "PCVC Chennai 24/7 domestic violence crisis helpline."),
        HelplineEntry("h_pcvc_dir", "PCVC Direct & Chat Support", "044-43111143", "Women", HelplineRegion.TAMIL_NADU, "Crisis intervention, legal aid, shelter. Chat: 9840888882."),
        HelplineEntry("h_speak2us", "Speak2Us Emotional Support", "9375493754", "Mental Health", HelplineRegion.TAMIL_NADU, "Tamil & English emotional support helpline."),
        HelplineEntry("h_sneha", "Sneha Suicide Prevention (Chennai)", "044-24640050", "Mental Health", HelplineRegion.TAMIL_NADU, "Chennai-based suicide prevention helpline."),
        HelplineEntry("h_cbe_sui", "Coimbatore Suicide Helpline", "8526565656", "Mental Health", HelplineRegion.TAMIL_NADU, "District suicide prevention helpline."),
        HelplineEntry("h_jeevan", "Jeevanline Toll-Free", "18002028760", "Mental Health", HelplineRegion.TAMIL_NADU, "Toll-free suicide prevention helpline."),
        HelplineEntry("h_stress", "Mental Stress Helpline (TN)", "9911599100", "Mental Health", HelplineRegion.TAMIL_NADU, "Tamil Nadu mental stress support helpline."),
        HelplineEntry("h_lgbtq_mdu", "LGBTQ+ Mental Health (Madurai)", "9360730317", "Mental Health", HelplineRegion.TAMIL_NADU, "Free mental health support for LGBTQIA+ individuals. Alt: 9965140064."),
        HelplineEntry("h_bandham", "Bandham Senior Helpline (75+)", "9499957575", "Senior Citizens", HelplineRegion.TAMIL_NADU, "Greater Chennai Police support for senior citizens 75+."),
        HelplineEntry("h_chn_sr", "Chennai Senior Citizen Helpline (60+)", "1253", "Senior Citizens", HelplineRegion.TAMIL_NADU, "Greater Chennai Police senior helpline. Toll-free: 1800-180-1253."),
        HelplineEntry("h_orinam", "Orinam (Chennai)", "9841557983", "LGBTQ+", HelplineRegion.TAMIL_NADU, "Chennai queer collective — resources and support."),
        HelplineEntry("h_nirangal", "Nirangal (Chennai)", "9840699776", "LGBTQ+", HelplineRegion.TAMIL_NADU, "Crisis support for sexual minorities."),
        HelplineEntry("h_tra", "Transgenders Right Association (Chennai)", "7667866669", "LGBTQ+", HelplineRegion.TAMIL_NADU, "Mx. Jeeva — legal crisis and social entitlements."),
        HelplineEntry("h_jh", "Justice and Hope Anti-Trafficking", "9500025000", "Anti-Trafficking", HelplineRegion.TAMIL_NADU, "24/7 anti-trafficking hotline. Main: 044-48565698."),
        HelplineEntry("h_iwid", "IWID Women's Organisation", "044-26222856", "Women", HelplineRegion.TAMIL_NADU, "Anna Nagar East, Chennai."),
        HelplineEntry("h_asset", "Asset Trust (Madurai)", "7708107859", "Women", HelplineRegion.TAMIL_NADU, "Manjanakara Street, Madurai – 625001."),
        HelplineEntry("h_cwdr", "Centre for Women's Development & Research", "044-24455868", "Women", HelplineRegion.TAMIL_NADU, "Thiruvanmiyur, Chennai."),
        HelplineEntry("h_wow", "Women of Worth (Chennai)", "9962818086", "Women", HelplineRegion.TAMIL_NADU, "Women's empowerment NGO, Chennai."),
        HelplineEntry("h_peace", "PEACE Trust (Tirunelveli)", "9489507132", "Women", HelplineRegion.TAMIL_NADU, "DV Act service provider, Palayamkottai, Tirunelveli."),
        HelplineEntry("h_dlsa_vlr", "DLSA Vellore Secretary", "9385472439", "Legal", HelplineRegion.TAMIL_NADU, "District Legal Services Authority, Vellore. OSC: 9944964467."),
        HelplineEntry("h_dlsa_dgl", "DLSA Dindigul (WhatsApp)", "9486627592", "Legal", HelplineRegion.TAMIL_NADU, "Free legal counselling via WhatsApp."),
        HelplineEntry("h_dlsa_tvl", "DLSA Tiruvallur (WhatsApp)", "9840760576", "Legal", HelplineRegion.TAMIL_NADU, "Free legal aid complaints via WhatsApp."),

        // ==================== SAKHI ONE STOP CENTRES (TN) ====================
        HelplineEntry("h_osc_chn1", "OSC Chennai – Centre Admin", "9677056919", "Women", HelplineRegion.TAMIL_NADU, "Mrs. Auxilia M. Christina, Sakhi One Stop Centre, Chennai."),
        HelplineEntry("h_osc_chn2", "OSC Chennai – Sr. Counsellor", "9551553535", "Women", HelplineRegion.TAMIL_NADU, "Dr. Malathy R, Senior Counsellor, Chennai."),
        HelplineEntry("h_osc_slm1", "OSC Salem – Centre Admin", "9677918795", "Women", HelplineRegion.TAMIL_NADU, "K. PeriyarSelvi, Centre Administrator, Salem."),
        HelplineEntry("h_osc_slm2", "OSC Salem – Sr. Counsellor", "9087058998", "Women", HelplineRegion.TAMIL_NADU, "S.P. Annam, Salem. Case Workers: 6369555507 / 8925458415."),
        HelplineEntry("h_osc_cbe1", "OSC Coimbatore – Centre Admin", "9791357905", "Women", HelplineRegion.TAMIL_NADU, "Mrs. A. Priyanka, Centre Administrator, Coimbatore."),
        HelplineEntry("h_osc_cbe2", "OSC Coimbatore – Sr. Counsellor", "9629360744", "Women", HelplineRegion.TAMIL_NADU, "Ms. J. Sherin Reshma, Coimbatore. Case Worker: 7708707756."),
        HelplineEntry("h_osc_try", "OSC Trichy – Sr. Counsellor", "7339130198", "Women", HelplineRegion.TAMIL_NADU, "Ms. Prakathi, Trichy. Case Workers: 9994394383 / 9344998273."),
        HelplineEntry("h_osc_kpm1", "OSC Kanchipuram – Centre Admin", "6379519465", "Women", HelplineRegion.TAMIL_NADU, "Ms. M. Abirami, Centre Administrator, Kanchipuram."),
        HelplineEntry("h_osc_kpm2", "OSC Kanchipuram – Sr. Counsellor", "8870775427", "Women", HelplineRegion.TAMIL_NADU, "Mrs. R. Amutha, Kanchipuram. Case Workers: 7358340024 / 7708448153."),
        HelplineEntry("h_osc_mdu", "OSC Madurai – Centre Admin", "9486229149", "Women", HelplineRegion.TAMIL_NADU, "P. Premalatha, Madurai. Case Worker: 9566822481."),
        HelplineEntry("h_osc_tvm1", "OSC Thiruvannamalai – Admin", "8098769388", "Women", HelplineRegion.TAMIL_NADU, "Mrs. A. Elizabethrani, Thiruvannamalai."),
        HelplineEntry("h_osc_tvm2", "OSC Thiruvannamalai – Counsellor", "9600022517", "Women", HelplineRegion.TAMIL_NADU, "Ms. P. Shanthini, Thiruvannamalai. Case Workers: 9943524634 / 9384652219."),
        HelplineEntry("h_osc_vnr", "OSC Virudhunagar – Sr. Counsellor", "9994605844", "Women", HelplineRegion.TAMIL_NADU, "M.P. Rachel Arumai, Virudhunagar. Case Worker: 9942037179."),
        HelplineEntry("h_osc_nil1", "OSC Nilgiris – Centre Admin", "9943040474", "Women", HelplineRegion.TAMIL_NADU, "Mrs. Helen Christeenal, Nilgiris."),
        HelplineEntry("h_osc_nil2", "OSC Nilgiris – Sr. Counsellor", "9843827626", "Women", HelplineRegion.TAMIL_NADU, "Ms. Kasthuri, Nilgiris. Case Worker: 6385346315."),
        HelplineEntry("h_osc_tpt", "OSC Tirupathur", "9159604087", "Women", HelplineRegion.TAMIL_NADU, "Sakhi One Stop Centre, Tirupathur."),
        HelplineEntry("h_osc_tpr", "OSC Tiruppur", "9150408101", "Women", HelplineRegion.TAMIL_NADU, "Sakhi One Stop Centre, Tiruppur."),
        HelplineEntry("h_osc_tvl", "OSC Tiruvallur", "8098822551", "Women", HelplineRegion.TAMIL_NADU, "Sakhi One Stop Centre, Tiruvallur."),
        HelplineEntry("h_osc_tvr", "OSC Tiruvarur", "8610373270", "Women", HelplineRegion.TAMIL_NADU, "Sakhi One Stop Centre, Tiruvarur."),
        HelplineEntry("h_osc_vlr", "OSC Vellore", "8508757670", "Women", HelplineRegion.TAMIL_NADU, "Sakhi One Stop Centre, Vellore."),
        HelplineEntry("h_osc_vpm", "OSC Viluppuram", "7871604171", "Women", HelplineRegion.TAMIL_NADU, "Sakhi One Stop Centre, Viluppuram."),

        // ==================== ALL 33 DV PROTECTION OFFICERS (TN) ====================
        HelplineEntry("h_po_ari", "Protection Officer – Ariyalur", "9842074680", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Velammal, District Social Welfare Office, Ariyalur."),
        HelplineEntry("h_po_try", "Protection Officer – Tiruchirappalli", "9626170321", "Legal", HelplineRegion.TAMIL_NADU, "Ms. Anandhi, Protection Officer, Trichy."),
        HelplineEntry("h_po_mdu", "Protection Officer – Madurai", "9567672932", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Rukmani, Protection Officer, Madurai."),
        HelplineEntry("h_po_chn", "Protection Officer – Chennai", "9786527453", "Legal", HelplineRegion.TAMIL_NADU, "Mr. Raghavan, Protection Officer, Chennai."),
        HelplineEntry("h_po_cbe", "Protection Officer – Coimbatore", "9865432109", "Legal", HelplineRegion.TAMIL_NADU, "Ms. Lakshmi, Protection Officer, Coimbatore."),
        HelplineEntry("h_po_dpi", "Protection Officer – Dharmapuri", "9094055559", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Sarasu, Protection Officer, Dharmapuri."),
        HelplineEntry("h_po_dgl", "Protection Officer – Dindigul", "9942511127", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Sudha, Protection Officer, Dindigul."),
        HelplineEntry("h_po_erd", "Protection Officer – Erode", "9865826081", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Ramayee, Protection Officer, Erode."),
        HelplineEntry("h_po_kpm", "Protection Officer – Kanchipuram", "9940801968", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Santhi Saraswathi, Protection Officer, Kanchipuram."),
        HelplineEntry("h_po_kni", "Protection Officer – Kanyakumari", "9488407667", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Visudha, Protection Officer, Kanyakumari."),
        HelplineEntry("h_po_krr", "Protection Officer – Karur", "9150368751", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Parvathi, Protection Officer, Karur."),
        HelplineEntry("h_po_kgi", "Protection Officer – Krishnagiri", "7904372355", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Thangamani, Protection Officer, Krishnagiri."),
        HelplineEntry("h_po_ngp", "Protection Officer – Nagapattinam", "8903392839", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Rama Maheswari, Protection Officer, Nagapattinam."),
        HelplineEntry("h_po_nmk", "Protection Officer – Namakkal", "9952507068", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Nagamani, Protection Officer, Namakkal."),
        HelplineEntry("h_po_nil", "Protection Officer – Nilgiris", "9943089307", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Anusha, Protection Officer, Nilgiris."),
        HelplineEntry("h_po_pmb", "Protection Officer – Perambalur", "9488018205", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Muthuselvi, Protection Officer, Perambalur."),
        HelplineEntry("h_po_pdk", "Protection Officer – Pudukottai", "9600941391", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Sumathi, Protection Officer, Pudukottai."),
        HelplineEntry("h_po_rmd", "Protection Officer – Ramanathapuram", "9751453179", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Nagajothi, Protection Officer, Ramanathapuram."),
        HelplineEntry("h_po_slm", "Protection Officer – Salem", "9894957867", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Kowsalya, Protection Officer, Salem."),
        HelplineEntry("h_po_svg", "Protection Officer – Sivagangai", "9629667716", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Sudha, Protection Officer, Sivagangai."),
        HelplineEntry("h_po_tnj", "Protection Officer – Thanjavur", "8012232577", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Ida Rajam, Protection Officer, Thanjavur."),
        HelplineEntry("h_po_thn", "Protection Officer – Theni", "9894854837", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Rajeswari, Protection Officer, Theni."),
        HelplineEntry("h_po_tvl", "Protection Officer – Tirunelveli", "8220382754", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Vanitha, Protection Officer, Tirunelveli."),
        HelplineEntry("h_po_tpr", "Protection Officer – Tiruppur", "9843654834", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Akilandeshwari, Protection Officer, Tiruppur."),
        HelplineEntry("h_po_tvm", "Protection Officer – Thiruvannamalai", "9840369614", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Gomathy, Protection Officer, Thiruvannamalai."),
        HelplineEntry("h_po_tvr", "Protection Officer – Thiruvarur", "8825669037", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Amutha, Protection Officer, Thiruvarur."),
        HelplineEntry("h_po_trl", "Protection Officer – Tiruvallur", "9976789017", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Pandiammal, Protection Officer, Tiruvallur."),
        HelplineEntry("h_po_tut", "Protection Officer – Tuticorin", "9585831850", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Selvamercy, Protection Officer, Tuticorin."),
        HelplineEntry("h_po_try2", "Protection Officer – Trichy (Flora)", "9843072080", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Flora Margaret, Protection Officer, Trichy."),
        HelplineEntry("h_po_vlr", "Protection Officer – Vellore", "9600394037", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Nithya, Protection Officer, Vellore."),
        HelplineEntry("h_po_vpm", "Protection Officer – Viluppuram", "9444930680", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Muthamil Sheela, Protection Officer, Viluppuram."),
        HelplineEntry("h_po_vnr", "Protection Officer – Virudhunagar", "9842835483", "Legal", HelplineRegion.TAMIL_NADU, "Mrs. Indra Jayasheeli, Protection Officer, Virudhunagar."),

        // ==================== HEALTH & CIVIC (TN) ====================
        HelplineEntry("h_lions", "Lions Blood Bank (Chennai)", "044-28415959", "Health", HelplineRegion.TAMIL_NADU, "Chennai blood bank."),
        HelplineEntry("h_stjohn", "St. Johns Ambulance (Chennai)", "044-28194630", "Health", HelplineRegion.TAMIL_NADU, "First aid and ambulance training/support."),
        HelplineEntry("h_tour", "Tamil Nadu Tourist Enquiry", "1913", "Emergency", HelplineRegion.TAMIL_NADU, "Tamil Nadu tourism assistance.")
    )

    val ORGANIZATIONS: List<OrganizationEntry> = listOf(
        OrganizationEntry("o_osc", "One Stop Centre (Sakhi)", "Government", HelplineRegion.TAMIL_NADU, "Tamil Nadu (59 OSCs)", "Integrated medical, police, legal, and shelter support for women facing violence.", "Gov"),
        OrganizationEntry("o_ms", "Mission Shakti Dashboard", "Government", HelplineRegion.NATIONAL, "National", "Consolidates helplines: 181, 15100, 1098, 112, 1930, 14416.", "Gov"),
        OrganizationEntry("o_ncw", "National Commission for Women (NCW)", "Government", HelplineRegion.NATIONAL, "New Delhi", "Apex statutory body running 24x7 helpline 14490 and 7827170170.", "Gov"),
        OrganizationEntry("o_tnswd", "TN Social Welfare Department", "Government", HelplineRegion.TAMIL_NADU, "Statewide", "Implements 181 WHL, Swadhar Homes, and DV Act service providers.", "Gov"),
        OrganizationEntry("o_pywc", "Puducherry Women's Commission", "Government", HelplineRegion.PUDUCHERRY, "Puducherry", "Statutory body for women's grievances and empowerment. Phone: 0413-2205153.", "Gov"),
        OrganizationEntry("o_tnslsa", "TNSLSA (TN Legal Services Authority)", "Government", HelplineRegion.TAMIL_NADU, "Statewide", "Free legal aid & representation: 15100 / 1800-4252-441 / 044-25342441.", "Gov"),
        OrganizationEntry("o_tntg", "TN Transgender Welfare Board", "Government", HelplineRegion.TAMIL_NADU, "Chennai (Saidapet)", "24/7 helpline under TN Transgender Policy 2025: 044-24351885.", "Gov"),
        OrganizationEntry("o_scd", "State Commissionerate for Differently Abled", "Government", HelplineRegion.TAMIL_NADU, "Chennai", "Welfare and support helpline: 18004250111.", "Gov"),
        OrganizationEntry("o_pcvc", "PCVC (Crime Prevention & Victim Care)", "NGO", HelplineRegion.TAMIL_NADU, "Chennai", "Survivor-centric crisis intervention, legal aid, and emergency shelter. Direct: 044-43111143.", "NGO"),
        OrganizationEntry("o_py_pest", "Prime Educational & Social Trust", "NGO", HelplineRegion.PUDUCHERRY, "Puducherry", "Microcredit, Janani Home for Girls, and vocational empowerment.", "NGO"),
        OrganizationEntry("o_py_psdf", "People Social Development Foundation", "NGO", HelplineRegion.PUDUCHERRY, "Puducherry", "Family Counselling Centre & women's empowerment. Dr. K. Mohanasundaram: 9443433927.", "NGO"),
        OrganizationEntry("o_py_pwc", "Pondicherry Women's Conference", "NGO", HelplineRegion.PUDUCHERRY, "Puducherry", "Family Counselling Centre and awareness camps. Mrs. Su. Rehana Begum: 8056709678.", "NGO"),
        OrganizationEntry("o_py_sharana", "Sharana Social & Development Org", "NGO", HelplineRegion.PUDUCHERRY, "Puducherry", "Back-to-school programmes, day care, and crèche facilities. 9843337640.", "NGO"),
        OrganizationEntry("o_py_flash", "FLASH (Foundation for Low Income Group)", "NGO", HelplineRegion.PUDUCHERRY, "Puducherry", "Home for women and children; free education. 9443500192.", "NGO"),
        OrganizationEntry("o_py_mangalam", "Mangalam Society", "NGO", HelplineRegion.PUDUCHERRY, "Puducherry (Rural)", "Gender justice, legal awareness, and support for rural women.", "NGO"),
        OrganizationEntry("o_py_anna", "Annalakshmi Trust", "NGO", HelplineRegion.PUDUCHERRY, "Puducherry", "Transgender-led organization providing job opportunities & community support.", "NGO"),
        OrganizationEntry("o_mccss", "Madras Christian Council of Social Service", "NGO", HelplineRegion.TAMIL_NADU, "Chennai (Perambur)", "DV Act service provider & family counselling.", "NGO"),
        OrganizationEntry("o_cfh", "Christ Faith Home", "NGO", HelplineRegion.TAMIL_NADU, "Chennai (Manapakkam)", "DV Act shelter for women in distress.", "NGO"),
        OrganizationEntry("o_imayam", "Imayam Social Welfare Association", "NGO", HelplineRegion.TAMIL_NADU, "Coimbatore", "DV Act service provider, Rathinapuri, Coimbatore.", "NGO"),
        OrganizationEntry("o_hope", "Fr. Patrick Issac Home of Hope", "NGO", HelplineRegion.TAMIL_NADU, "Madurai", "DV Act shelter for women and children.", "NGO"),
        OrganizationEntry("o_word", "WORD (Women's Org for Rural Development)", "NGO", HelplineRegion.TAMIL_NADU, "Namakkal", "DV Act service provider, Namakkal District.", "NGO"),
        OrganizationEntry("o_saras", "Saras Trust", "NGO", HelplineRegion.TAMIL_NADU, "Ooty (Nilgiris)", "DV Act service provider, Nilgiris District.", "NGO"),
        OrganizationEntry("o_avvai", "Avvai Village Welfare Society", "NGO", HelplineRegion.TAMIL_NADU, "Nagapattinam", "DV Act service provider, Nagapattinam.", "NGO"),
        OrganizationEntry("o_creed", "CREED Family Counselling Centre", "NGO", HelplineRegion.TAMIL_NADU, "Cuddalore (Chidambaram)", "DV Act service provider, Chidambaram.", "NGO"),
        OrganizationEntry("o_ashraya", "Ashraya (Andhra Mahila Sabha)", "NGO", HelplineRegion.TAMIL_NADU, "Chennai", "Shelter and support for women facing domestic violence since 1986.", "NGO"),
        OrganizationEntry("o_ruwsec", "RUWSEC", "NGO", HelplineRegion.TAMIL_NADU, "Chengalpattu", "Community-based interventions preventing domestic violence since 1981.", "NGO"),
        OrganizationEntry("o_anew", "Anew Foundation", "NGO", HelplineRegion.TAMIL_NADU, "Chennai", "Vocational training & support for DV survivors (18,000+ women trained).", "NGO"),
        OrganizationEntry("o_shakti", "Shakti Shalini", "NGO", HelplineRegion.OTHER, "New Delhi", "Response to survivors of gender/sexual violence. Helpline: 10920.", "NGO"),
        OrganizationEntry("o_majlis", "Majlis Law", "NGO", HelplineRegion.OTHER, "Mumbai", "Legal aid and court support for women facing violence. Helpline: 07506732641.", "NGO"),
        OrganizationEntry("o_mychoices", "My Choices Foundation", "NGO", HelplineRegion.OTHER, "Hyderabad (National)", "Domestic violence prevention and peace-making programmes.", "NGO"),
        OrganizationEntry("o_raahi", "RAHI Foundation", "NGO", HelplineRegion.OTHER, "New Delhi", "Support centre for women survivors of child sexual abuse.", "NGO")
    )

    val LEGAL_RIGHTS: List<LegalRightEntry> = listOf(
        LegalRightEntry(
            "l_pocso",
            "POCSO Act, 2012 — Protection of Children from Sexual Offences",
            "Protects all children under 18 from sexual abuse. It is gender-neutral with child-friendly in-camera trials and strict penalties up to life imprisonment or death.",
            "1098"
        ),
        LegalRightEntry(
            "l_dv",
            "Protection of Women from Domestic Violence Act, 2005",
            "Protects women from physical, emotional, verbal, sexual, and economic abuse at home. Provides protection orders, residence orders, and monetary relief via District Protection Officers.",
            "181"
        ),
        LegalRightEntry(
            "l_posh",
            "Sexual Harassment at Workplace (PoSH) Act, 2013",
            "Every workplace with 10+ employees must maintain an Internal Complaints Committee (ICC). You can file confidential workplace harassment complaints directly with the ICC or Local Committee.",
            "181"
        ),
        LegalRightEntry(
            "l_legalaid",
            "Free Legal Aid — No Fees Required",
            "Women and children are automatically eligible for free legal representation under Legal Services Authorities Act. Call National Legal Aid 15100 or TNSLSA 1800-4252-441.",
            "15100"
        ),
        LegalRightEntry(
            "l_childmarriage",
            "Prohibition of Child Marriage Act, 2006",
            "Marrying a girl below 18 or a boy below 21 is a punishable offence. Report immediately to Childline 1098 or Emergency 112.",
            "1098"
        ),
        LegalRightEntry(
            "l_missing",
            "Missing Women & Children — Helpline 1094",
            "If a woman or child goes missing, dial 1094 (TN dedicated missing persons helpline) or 112 immediately without waiting 24 hours.",
            "1094"
        ),
        LegalRightEntry(
            "l_tg",
            "Transgender Rights — TN Policy 2025",
            "Affirms the right to self-identify gender without medical certification, mandates a 24/7 helpline (044-24351885), free legal aid, and family counselling.",
            "044-24351885"
        ),
        LegalRightEntry(
            "l_cyber",
            "Cybercrime & Online Safety Rights",
            "Report cyberstalking, morphing, blackmail, or financial fraud immediately at cybercrime.gov.in or toll-free 1930 (Golden Hour freeze) / Puducherry Cyber Cell 155260.",
            "1930"
        )
    )

    val CASE_STUDIES: List<CaseStudyEntry> = listOf(
        CaseStudyEntry("c_1", "Tamil Nadu · 2025", "Tirunelveli POCSO Case — Strict Conviction", "A POCSO court sentenced an offender to the maximum statutory penalty and ordered ₹10 lakh state compensation for the survivor, reinforcing zero tolerance for child abuse."),
        CaseStudyEntry("c_2", "Tamil Nadu · 2025", "Tiruvarur POCSO Case — Gender-Neutral Law", "A 54-year rigorous imprisonment sentence proved the POCSO Act applies strictly regardless of offender gender."),
        CaseStudyEntry("c_3", "Tamil Nadu · 2025", "Nilgiris School Awareness Breakthrough", "Abuse came to light only after police conducted a school awareness session on Good Touch and Bad Touch, empowering students to report."),
        CaseStudyEntry("c_4", "India · 2012", "Criminal Law Amendment Turning Point", "Nationwide advocacy led to fast-track courts, stronger stalking/voyeurism offences, and expanded victim support services."),
        CaseStudyEntry("c_5", "Panchkula · 2025", "School Workshop Empowers Teen to Report", "A 14-year-old student found the courage to report daily harassment during her commute after attending a body-safety workshop."),
        CaseStudyEntry("c_6", "Global · BRAC", "Shapla's Story — 20 Minutes That Saved a Child", "Remembering the 'Bathing Suit Rule' and 'No, Go, Tell' action plan from a 20-minute classroom lesson enabled an 8-year-old to escape harm."),
        CaseStudyEntry("c_7", "Tamil Nadu · 2025", "NIMIR Police Team — Child-Friendly Policing", "Special police educators travel across districts teaching students personal boundaries and safe reporting."),
        CaseStudyEntry("c_8", "Chennai · 2025", "Mandatory School Safeguarding Committees", "Private schools issued POCSO training mandates for all staff, parent sessions, and school safeguarding committees.")
    )

    val LEARNING_MODULES: List<LearningModule> = listOf(
        LearningModule(
            id = "goodtouch",
            badgeId = "badge_goodtouch",
            emoji = "🤗",
            titleEn = "Good Touch & Bad Touch",
            titleTa = "நல்ல தொடுதல் & தவறான தொடுதல்",
            tag = "BODY SAFETY",
            shortDescEn = "Learn the difference and who to tell immediately.",
            meenaTipEn = "Good touch feels safe and caring. Bad touch feels scary or uncomfortable. Private parts (swimsuit area) are yours — say NO loudly, run away, and tell a trusted adult!",
            meenaTipTa = "பாதுகாப்பான தொடுதல் மகிழ்ச்சியைத் தரும். பயம் தரும் தொடுதலுக்கு சத்தமாக 'வேண்டாம்' எனச் சொல்லி நம்பிக்கையான பெரியவரிடம் கூறுங்கள்.",
            keyPointsEn = listOf(
                "Good touch makes you feel safe, respected, and cared for (e.g., parent's hug, friend's high-five).",
                "Bad touch makes you feel scared, confused, or uncomfortable — especially touching private areas covered by a swimsuit.",
                "If someone gives a bad touch: Say NO loudly, Move away to safety, and Tell a trusted adult right away."
            ),
            rememberBox = "Remember: Bad touch is NEVER your fault, even if the person is someone you know. Keep telling trusted adults until someone helps.",
            diagramType = "swimsuit"
        ),
        LearningModule(
            id = "pants",
            badgeId = "badge_pants",
            emoji = "🛡️",
            titleEn = "The PANTS Rule",
            titleTa = "PANTS பாதுகாப்பு விதி",
            tag = "5 SIMPLE RULES",
            shortDescEn = "5 simple rules to protect your body.",
            meenaTipEn = "P: Privates are private · A: Always your body belongs to you · N: No means no · T: Talk about secrets that upset you · S: Speak up!",
            meenaTipTa = "P: அந்தரங்க உறுப்புகள் தனிப்பட்டவை · A: உங்கள் உடல் உங்களுடையது · N: வேண்டாம் என்றால் வேண்டாம் · T: பயமுறுத்தும் ரகசியங்களை சொல்லுங்கள் · S: பேசுங்கள்!",
            keyPointsEn = listOf(
                "P — Privates are private: Parts covered by underwear should not be touched or photographed.",
                "A — Always remember your body belongs to you: You are the boss of your body.",
                "N — No means no: You can say NO to any touch you dislike.",
                "T — Talk about secrets that upset you: Never keep scary secrets.",
                "S — Speak up: A trusted adult or Childline 1098 will help."
            ),
            rememberBox = "Medical exception: Only a doctor or nurse may examine you when a parent/guardian is present and explains why.",
            diagramType = "pants"
        ),
        LearningModule(
            id = "cybersafety",
            badgeId = "badge_cyber",
            emoji = "📱",
            titleEn = "Cyber Safety",
            titleTa = "இணைய பாதுகாப்பு",
            tag = "ONLINE SAFETY",
            shortDescEn = "Stay safe on phones, games, and social media.",
            meenaTipEn = "Never share your full name, school, photos, or passwords with strangers online. Report cyberbullying or blackmail to 1930 or cybercrime.gov.in!",
            meenaTipTa = "ஆன்லைனில் முன்பின் தெரியாதவர்களுடன் முகவரி, பள்ளி அல்லது கடவுச்சொல்லைப் பகிராதீர்கள். சைபர் புகார்க்கு 1930-ஐ அழைக்கவும்.",
            keyPointsEn = listOf(
                "Never share your full name, home address, school, or phone number with strangers online.",
                "Keep passwords private and enable two-factor authentication.",
                "Do not reply to threatening or creepy messages — screenshot them and show a trusted adult.",
                "Never meet an online stranger alone.",
                "Dial 1930 (24/7 Cyber Crime Helpline) or file at cybercrime.gov.in immediately."
            ),
            rememberBox = "Golden Hour Rule: For financial OTP/UPI scams or cyber harassment, calling 1930 immediately helps freeze fraud and document evidence.",
            diagramType = "cyber"
        ),
        LearningModule(
            id = "trustedadults",
            badgeId = "badge_trusted",
            emoji = "🤝",
            titleEn = "Trusted Adults & Safety Circle",
            titleTa = "நம்பிக்கையான பெரியவர்கள்",
            tag = "SAFETY CIRCLE",
            shortDescEn = "Know your 5 safety circle people when you feel unsafe.",
            meenaTipEn = "Your Safety Circle includes Mom, Dad, Grandmother, Teacher/Counsellor, and Police. If the first adult doesn't believe you, tell another!",
            meenaTipTa = "அம்மா, அப்பா, பாட்டி, ஆசிரியர், காவல்துறை ஆகியோர் உங்கள் பாதுகாப்பு வட்டம். ஒருவர் நம்பாவிட்டால் மற்றவரிடம் சொல்லுங்கள்.",
            keyPointsEn = listOf(
                "Identify 5 trusted adults across home, school, and community.",
                "Trusted adults listen calmly, believe you, and take action to keep you safe.",
                "If the first adult you tell does not act, tell another trusted adult immediately.",
                "Childline 1098 and Women Helpline 181 are always free and confidential."
            ),
            rememberBox = "You deserve to be heard and protected every single time.",
            diagramType = "circle"
        ),
        LearningModule(
            id = "consent",
            badgeId = "badge_consent",
            emoji = "✋",
            titleEn = "My Body, My Rules",
            titleTa = "என் உடல், என் உரிமை",
            tag = "CONSENT",
            shortDescEn = "Understanding consent and personal boundaries.",
            meenaTipEn = "Consent means asking first and respecting 'NO'. You never have to hug or sit near anyone if it makes you uncomfortable.",
            meenaTipTa = "சம்மதம் என்பது அனுமதி கேட்பதும் 'வேண்டாம்' என்பதை மதிப்பதும் ஆகும்.",
            keyPointsEn = listOf(
                "You have the right to say NO to any uncomfortable touch — even from relatives or acquaintances.",
                "Respecting boundaries goes both ways: always ask before hugging or touching friends.",
                "Saying NO to stay safe is never rude — it is your legal and human right."
            ),
            rememberBox = "Practice saying clearly: 'Please stop, I do not like that.'",
            diagramType = "consent"
        ),
        LearningModule(
            id = "secrets",
            badgeId = "badge_secrets",
            emoji = "🤫",
            titleEn = "Safe vs. Unsafe Secrets",
            titleTa = "பாதுகாப்பான vs ஆபத்தான ரகசியங்கள்",
            tag = "SECRETS",
            shortDescEn = "Some secrets should never be kept.",
            meenaTipEn = "Surprise birthday gifts are safe secrets. Any secret that makes your stomach hurt or involves threats or touching is an UNSAFE secret — tell immediately!",
            meenaTipTa = "பிறந்தநாள் ஆச்சரியம் பாதுகாப்பான ரகசியம். பயம் அல்லது மிரட்டல் உள்ள ரகசியத்தை உடனே பெரியவர்களிடம் கூறுங்கள்.",
            keyPointsEn = listOf(
                "Safe secrets are happy, temporary surprises (like a birthday gift) that never hurt anyone.",
                "Unsafe secrets make you feel scared, guilty, threatened, or sick inside.",
                "Never keep a secret about touching, photos, or threats — you will NOT get in trouble for telling."
            ),
            rememberBox = "Rule: Threats like 'Don't tell anyone or you'll be in trouble' are proof you MUST tell a trusted adult immediately.",
            diagramType = "secrets"
        )
    )

    val QUIZ_QUESTIONS: List<QuizQuestion> = listOf(
        QuizQuestion(1, "🤗", "A teacher pats you on the back for good work.", "நல்ல வேலைக்காக ஆசிரியர் முதுகில் தட்டிக்கொடுக்கிறார்.", "Good Touch", "Praise on the shoulder/back in public feels encouraging and safe."),
        QuizQuestion(2, "🚫", "A stranger asks you to sit on their lap.", "அறிமுகமில்லாத நபர் தன் மடியில் உட்காரச் சொல்கிறார்.", "Bad Touch", "Never sit on a stranger's lap or let anyone cross personal boundaries. Say NO and move away."),
        QuizQuestion(3, "👨‍👧", "Your parent hugs you when you are sad.", "நீங்கள் சோகமாக இருக்கும்போது பெற்றோர் அணைக்கிறார்கள்.", "Good Touch", "Caring comfort from a parent makes you feel safe and loved."),
        QuizQuestion(4, "📱", "Someone online asks for your home address.", "இணையத்தில் ஒருவர் உங்கள் வீட்டு முகவரியைக் கேட்கிறார்.", "Not Safe", "Never share home addresses, school names, or photos online. Show a parent and call 1930 if threatened."),
        QuizQuestion(5, "👩‍⚕️", "A doctor checks you with your mother in the room.", "உங்கள் அம்மா அருகில் இருக்கும்போது மருத்துவர் பரிசோதிக்கிறார்.", "Good Touch", "Medical checks with a trusted parent present and clear explanation are safe."),
        QuizQuestion(6, "🤫", "An adult says, 'Don't tell anyone about this secret.'", "ஒரு பெரியவர், 'இந்த ரகசியத்தை யாரிடமும் சொல்லாதே' என்கிறார்.", "Bad Touch", "Unsafe secrets that involve fear or hiding are never okay. Tell a trusted adult immediately."),
        QuizQuestion(7, "🤝", "Your friend gives you a high-five.", "உங்கள் நண்பர் ஹை-ஃபைவ் கொடுக்கிறார்.", "Good Touch", "A friendly high-five is fun, public, and respectful.")
    )

    val SAFE_ZONES: List<SafeZoneEntry> = listOf(
        SafeZoneEntry("sz_py_awps", "All Women PS – Puducherry", HelplineRegion.PUDUCHERRY, "Police", "0413-2336178", "Puducherry Town Women Police Station · 24/7 assistance", 11.9338, 79.8308),
        SafeZoneEntry("sz_py_gb", "Grand Bazaar Police Station", HelplineRegion.PUDUCHERRY, "Police", "112", "Central Puducherry Town Patrol Hub", 11.9330, 79.8290),
        SafeZoneEntry("sz_py_lawspet", "Lawspet Police Station", HelplineRegion.PUDUCHERRY, "Police", "112", "Lawspet Junction Emergency Response", 11.9470, 79.8180),
        SafeZoneEntry("sz_py_vill", "All Women PS – Villianur", HelplineRegion.PUDUCHERRY, "Police", "0413-2660385", "Villianur Sector · Mobile: 94421-07748", 11.9130, 79.7600),
        SafeZoneEntry("sz_tn_chn", "All Women PS – Chennai (City)", HelplineRegion.TAMIL_NADU, "Police", "1091", "Dedicated Chennai Women Police Helpline · 044-23452365", 13.0827, 80.2707),
        SafeZoneEntry("sz_tn_osc_chn", "One Stop Centre (Sakhi) – Chennai", HelplineRegion.TAMIL_NADU, "One Stop Centre", "9677056919", "Integrated medical, legal, police & shelter aid", 13.0674, 80.2376),
        SafeZoneEntry("sz_tn_cbe", "All Women PS – Coimbatore", HelplineRegion.TAMIL_NADU, "Police", "0422-2300818", "Coimbatore City Women Police · 2305126", 11.0168, 76.9558),
        SafeZoneEntry("sz_tn_mdu", "All Women PS – Madurai", HelplineRegion.TAMIL_NADU, "Police", "0452-2343602", "Crime Prevention Cell, Madurai City", 9.9252, 78.1198),
        SafeZoneEntry("sz_tn_slm", "All Women PS – Salem", HelplineRegion.TAMIL_NADU, "Police", "9087200100", "Collector Office Campus, Salem", 11.6643, 78.1460)
    )
}
