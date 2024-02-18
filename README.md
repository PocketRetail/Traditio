# Important
UI is still in development, so it's not ready for use yet.

## What is this?
Traditio is an GraphQL DeliveryLayer which is based on Spring Boot and DGS Framework. It get your current Queries from your Client if they register at Traditio and will refresh them at any registration (so anytime if you start your Client). 

Because of the fact that the UI wont be ready in the next few weeks, you can configure the Pages just in Database which isnt so nice but it works. The plan is also to split UI and Traditio in two different projects, so you can use Traditio without the UI in future.

## What will be finished soon?
- [ ] Page creation in Database
- [ ] Page request configuration with specific parameters
- [ ] load page and merge different response to one
- [ ] generate from database page configuration a valid GraphQL Query
