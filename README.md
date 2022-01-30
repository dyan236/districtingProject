# Districting Web App Project
A single-page React web application designed to provide the user with a succinct, summary analysis of proposed districting plans to determine the "political fairness" of each plan, and assist in selecting fair plans to avoid gerrymandering.
Given numerous districting plans, the application grades each one according to an objective function and provides a listing of which the user can select from to take a closer look at individual plans. 
The application revolves around using four different tabs to narrow down and then inspect potentially interesting districting plans. 

## Filter Tab
Allows the user to apply a filter on a few immediate metrics to quickly narrow down a large number of districting plans. 
## Objective Function Tab
Allows the user to set the weights of each individual scoring category, resulting in the overall score for each plan able to be adjusted to the user's liking; If, for example, having equal population in each district was more important than having compact districts. 
## Results Tab
Provides a summary overview of all districting plans that passed the filter, and provides a small subset for the user to choose to inspect. The user can choose what subset they are interested in, ranging from the top scoring plans to plans with exactly the amount of desired majority-minority districts. 
## Details Tab
Once the user chooses a district to inspect, this tab provides more information regarding how exactly the plan's overall score was calculated and other miscellaneous metrics that could be helpful in determining if the currently selected plan is fair. 

## Back-End
The back-end of the project is comprised of a Spring server, which connected to a database hosted at Stony Brook University. The server was responsible for handling http requests from the React application and responding accordingly; This ranged from retrieving data from the external database or running calculations to provide data needed by the web application.
