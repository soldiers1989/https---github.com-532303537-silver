package org.silver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value="/")
public class HomeController {
	 @RequestMapping(value="/unauthorized")
	 public String unauthorized(){
		 
		 return null;
	 }
}
